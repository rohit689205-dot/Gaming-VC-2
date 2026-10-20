package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.RoomMember
import com.example.model.VcRoom
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.random.Random

class RoomRepository(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    suspend fun createRoom(
        roomName: String,
        privacyMode: String,
        hostUserId: String,
        hostName: String
    ): Result<VcRoom> {
        val roomId = UUID.randomUUID().toString().take(12)
        val inviteCode = (100000 + Random.nextInt(900000)).toString()
        val roomPath = "rooms/$roomId"

        return try {
            val vcRoom = VcRoom(
                id = roomId,
                roomId = roomId,
                roomName = roomName.ifBlank { "Voice Room" },
                hostUserId = hostUserId,
                hostName = hostName.ifBlank { "Host" },
                inviteCode = inviteCode,
                privacyMode = privacyMode,
                status = "active",
                participantCount = 1
            )

            val payload = mapOf(
                "roomId" to roomId,
                "roomName" to vcRoom.roomName,
                "hostUserId" to hostUserId,
                "hostName" to vcRoom.hostName,
                "inviteCode" to inviteCode,
                "privacyMode" to privacyMode,
                "status" to "active",
                "participantCount" to 1,
                "createdAt" to FieldValue.serverTimestamp()
            )

            db.collection("rooms").document(roomId).set(payload).await()

            // Add host as first member
            val member = RoomMember(
                id = hostUserId,
                userId = hostUserId,
                displayName = vcRoom.hostName,
                isMuted = false,
                isSpeaking = false
            )
            val memberPayload = mapOf(
                "userId" to hostUserId,
                "displayName" to vcRoom.hostName,
                "isMuted" to false,
                "isSpeaking" to false,
                "joinedAt" to FieldValue.serverTimestamp()
            )
            db.collection("rooms").document(roomId)
                .collection("members").document(hostUserId)
                .set(memberPayload).await()

            Result.success(vcRoom)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, roomPath)
            Result.failure(e)
        }
    }

    suspend fun joinRoomByCode(
        inviteCode: String,
        userId: String,
        displayName: String
    ): Result<VcRoom> {
        val cleanCode = inviteCode.trim().filter { it.isDigit() }
        val path = "rooms"

        return try {
            val querySnapshot = db.collection("rooms")
                .whereEqualTo("inviteCode", cleanCode)
                .whereEqualTo("status", "active")
                .get().await()

            if (querySnapshot.isEmpty) {
                return Result.failure(Exception("Meeting code not found or room has ended."))
            }

            val doc = querySnapshot.documents.first()
            val room = doc.toObject(VcRoom::class.java) ?: return Result.failure(Exception("Failed parsing room document."))

            // Add user as member
            val memberPayload = mapOf(
                "userId" to userId,
                "displayName" to displayName.ifBlank { "Participant" },
                "isMuted" to false,
                "isSpeaking" to false,
                "joinedAt" to FieldValue.serverTimestamp()
            )
            db.collection("rooms").document(room.roomId)
                .collection("members").document(userId)
                .set(memberPayload).await()

            // Increment count
            db.collection("rooms").document(room.roomId)
                .update("participantCount", FieldValue.increment(1)).await()

            Result.success(room)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    fun observeRoomMembers(roomId: String): Flow<List<RoomMember>> {
        val path = "rooms/$roomId/members"
        return db.collection("rooms").document(roomId).collection("members")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(RoomMember::class.java)
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                emit(emptyList())
            }
    }

    suspend fun updateMemberState(
        roomId: String,
        userId: String,
        isMuted: Boolean,
        isSpeaking: Boolean
    ) {
        val path = "rooms/$roomId/members/$userId"
        try {
            db.collection("rooms").document(roomId)
                .collection("members").document(userId)
                .update(
                    mapOf(
                        "isMuted" to isMuted,
                        "isSpeaking" to isSpeaking
                    )
                ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
        }
    }

    suspend fun leaveRoom(roomId: String, userId: String) {
        val path = "rooms/$roomId/members/$userId"
        try {
            db.collection("rooms").document(roomId)
                .collection("members").document(userId)
                .delete().await()

            db.collection("rooms").document(roomId)
                .update("participantCount", FieldValue.increment(-1)).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
        }
    }

    suspend fun endRoom(roomId: String) {
        val path = "rooms/$roomId"
        try {
            db.collection("rooms").document(roomId)
                .update(
                    mapOf(
                        "status" to "ended",
                        "endedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
        }
    }
}
