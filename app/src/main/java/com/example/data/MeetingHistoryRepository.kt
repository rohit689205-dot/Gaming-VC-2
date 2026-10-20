package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.MeetingHistoryItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class MeetingHistoryRepository(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private val _guestHistoryState = MutableStateFlow<List<MeetingHistoryItem>>(emptyList())

    suspend fun addMeetingHistory(userId: String, item: MeetingHistoryItem) {
        if (userId.isBlank()) return
        
        val currentGuestList = _guestHistoryState.value.toMutableList()
        currentGuestList.add(0, item)
        _guestHistoryState.value = currentGuestList

        val currentUser = auth.currentUser
        if (currentUser == null || userId.startsWith("guest_") || currentUser.uid != userId) {
            return
        }

        val docPath = "users/$userId/meetingHistory"
        try {
            val payload = mapOf(
                "roomId" to item.roomId,
                "roomName" to item.roomName,
                "hostUserId" to item.hostUserId,
                "hostName" to item.hostName,
                "inviteCode" to item.inviteCode,
                "durationMinutes" to item.durationMinutes,
                "status" to item.status,
                "participantCount" to item.participantCount,
                "joinedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(userId)
                .collection("meetingHistory")
                .add(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
        }
    }

    fun observeMeetingHistory(userId: String): Flow<List<MeetingHistoryItem>> {
        val currentUser = auth.currentUser
        if (currentUser == null || userId.startsWith("guest_") || currentUser.uid != userId) {
            return _guestHistoryState.asStateFlow()
        }

        val path = "users/$userId/meetingHistory"
        return db.collection("users").document(userId)
            .collection("meetingHistory")
            .orderBy("joinedAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(MeetingHistoryItem::class.java)
                }
            }
            .catch { error ->
                Log.w("MeetingHistoryRepo", "Firestore meeting history observation error: ${error.message}")
                emit(_guestHistoryState.value)
            }
    }
}
