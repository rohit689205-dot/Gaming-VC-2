package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.FriendRequest
import com.example.model.Friendship
import com.example.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FriendRepository(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private val _localIncomingState = MutableStateFlow<List<FriendRequest>>(emptyList())
    private val _localOutgoingState = MutableStateFlow<List<FriendRequest>>(emptyList())
    private val _localFriendsState = MutableStateFlow<List<Friendship>>(emptyList())

    fun observeAllUsers(currentUserId: String): Flow<List<UserProfile>> {

        return db.collection("users")
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { doc -> doc.toObject(UserProfile::class.java) }
                    .filter { user -> user.id != currentUserId }
            }
            .catch { e ->
                Log.w("FriendRepo", "Error observing users: ${e.message}")
                emit(emptyList())
            }
    }

    suspend fun searchUsers(query: String, currentUserId: String): Result<List<UserProfile>> {

        val cleanQuery = query.trim().lowercase().removePrefix("@")
        val path = "users"
        return try {
            val allUsersSnapshot = db.collection("users").get().await()
            val allUsers = allUsersSnapshot.documents
                .mapNotNull { doc -> doc.toObject(UserProfile::class.java) }
                .filter { user -> user.id != currentUserId }

            val matchedList = if (cleanQuery.isBlank()) {
                allUsers
            } else {
                allUsers.filter { user ->
                    user.username.lowercase().contains(cleanQuery) ||
                    user.displayName.lowercase().contains(cleanQuery) ||
                    user.id.lowercase() == cleanQuery
                }
            }
            Result.success(matchedList)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    suspend fun sendFriendRequest(sender: UserProfile, recipient: UserProfile): Result<Unit> {
        if (sender.id == recipient.id) {
            return Result.failure(Exception("You cannot send a friend request to yourself."))
        }
        val path = "friendRequests"

        return try {
            val existingSnapshot = db.collection("friendRequests")
                .whereEqualTo("senderId", sender.id)
                .whereEqualTo("recipientId", recipient.id)
                .whereEqualTo("status", "pending")
                .get().await()

            if (!existingSnapshot.isEmpty) {
                return Result.failure(Exception("Friend request already pending."))
            }

            val requestId = "${sender.id}_${recipient.id}"
            val payload = mapOf(
                "senderId" to sender.id,
                "senderName" to sender.displayName.ifBlank { "Gamer" },
                "senderUsername" to sender.username.ifBlank { "@gamer" },
                "senderAvatar" to sender.avatarUrl,
                "recipientId" to recipient.id,
                "recipientName" to recipient.displayName.ifBlank { "Gamer" },
                "recipientUsername" to recipient.username.ifBlank { "@gamer" },
                "recipientAvatar" to recipient.avatarUrl,
                "status" to "pending",
                "createdAt" to FieldValue.serverTimestamp()
            )

            db.collection("friendRequests").document(requestId).set(payload).await()

            val newReq = FriendRequest(
                id = requestId,
                senderId = sender.id,
                senderName = sender.displayName,
                senderUsername = sender.username,
                recipientId = recipient.id,
                recipientName = recipient.displayName,
                recipientUsername = recipient.username,
                status = "pending"
            )
            val updatedOutgoing = _localOutgoingState.value.toMutableList()
            updatedOutgoing.add(0, newReq)
            _localOutgoingState.value = updatedOutgoing

            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun observeIncomingRequests(userId: String): Flow<List<FriendRequest>> {
        if (userId.isBlank()) {
            return _localIncomingState.asStateFlow()
        }

        return db.collection("friendRequests")
            .whereEqualTo("recipientId", userId)
            .whereEqualTo("status", "pending")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FriendRequest::class.java)
                }
            }
            .catch { error ->
                Log.w("FriendRepo", "Incoming requests error: ${error.message}")
                emit(_localIncomingState.value)
            }
    }

    fun observeOutgoingRequests(userId: String): Flow<List<FriendRequest>> {
        if (userId.isBlank()) {
            return _localOutgoingState.asStateFlow()
        }

        return db.collection("friendRequests")
            .whereEqualTo("senderId", userId)
            .whereEqualTo("status", "pending")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FriendRequest::class.java)
                }
            }
            .catch { error ->
                Log.w("FriendRepo", "Outgoing requests error: ${error.message}")
                emit(_localOutgoingState.value)
            }
    }

    fun observeFriends(userId: String): Flow<List<Friendship>> {
        if (userId.isBlank()) {
            return _localFriendsState.asStateFlow()
        }

        return db.collection("friendships")
            .whereEqualTo("user1Id", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Friendship::class.java)
                }
            }
            .catch { error ->
                Log.w("FriendRepo", "Friends list observation error: ${error.message}")
                emit(_localFriendsState.value)
            }
    }

    suspend fun acceptFriendRequest(request: FriendRequest): Result<Unit> {
        val path = "friendRequests/${request.id}"
        return try {
            db.collection("friendRequests").document(request.id)
                .update("status", "accepted").await()

            val friendship1Id = "${request.senderId}_${request.recipientId}"
            val friendship2Id = "${request.recipientId}_${request.senderId}"

            val f1Payload = mapOf(
                "user1Id" to request.recipientId,
                "user2Id" to request.senderId,
                "friendUserId" to request.senderId,
                "friendDisplayName" to request.senderName,
                "friendUsername" to request.senderUsername,
                "friendAvatarUrl" to request.senderAvatar,
                "isOnline" to true,
                "activityStatus" to "Playing Valorant",
                "lastSeenText" to "Just now",
                "createdAt" to FieldValue.serverTimestamp()
            )

            val f2Payload = mapOf(
                "user1Id" to request.senderId,
                "user2Id" to request.recipientId,
                "friendUserId" to request.recipientId,
                "friendDisplayName" to request.recipientName,
                "friendUsername" to request.recipientUsername,
                "friendAvatarUrl" to request.recipientAvatar,
                "isOnline" to true,
                "activityStatus" to "Playing Apex Legends",
                "lastSeenText" to "Just now",
                "createdAt" to FieldValue.serverTimestamp()
            )

            db.collection("friendships").document(friendship1Id).set(f1Payload).await()
            db.collection("friendships").document(friendship2Id).set(f2Payload).await()

            val newFriend = Friendship(
                id = friendship1Id,
                user1Id = request.recipientId,
                user2Id = request.senderId,
                friendUserId = request.senderId,
                friendDisplayName = request.senderName,
                friendUsername = request.senderUsername,
                isOnline = true,
                activityStatus = "Playing Valorant",
                lastSeenText = "Just now"
            )
            val updatedFriends = _localFriendsState.value.toMutableList()
            updatedFriends.add(0, newFriend)
            _localFriendsState.value = updatedFriends

            val updatedIncoming = _localIncomingState.value.filter { it.id != request.id }
            _localIncomingState.value = updatedIncoming

            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun declineFriendRequest(requestId: String): Result<Unit> {
        val path = "friendRequests/$requestId"
        return try {
            db.collection("friendRequests").document(requestId)
                .update("status", "declined").await()

            val updatedIncoming = _localIncomingState.value.filter { it.id != requestId }
            _localIncomingState.value = updatedIncoming

            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun cancelFriendRequest(requestId: String): Result<Unit> {
        val path = "friendRequests/$requestId"
        return try {
            db.collection("friendRequests").document(requestId).delete().await()

            val updatedOutgoing = _localOutgoingState.value.filter { it.id != requestId }
            _localOutgoingState.value = updatedOutgoing

            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun removeFriend(user1Id: String, user2Id: String): Result<Unit> {
        val f1Id = "${user1Id}_$user2Id"
        val f2Id = "${user2Id}_$user1Id"
        return try {
            db.collection("friendships").document(f1Id).delete().await()
            db.collection("friendships").document(f2Id).delete().await()

            val updatedFriends = _localFriendsState.value.filter { it.friendUserId != user2Id }
            _localFriendsState.value = updatedFriends

            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "friendships/$f1Id")
            Result.failure(e)
        }
    }

    suspend fun sendHeartbeat(userId: String) {
        if (userId.isBlank()) return
        try {
            val statusMap = mapOf<String, Any>(
                "isOnline" to true,
                "lastActive" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(userId).update(statusMap).await()

            val friendshipsSnapshot = db.collection("friendships")
                .whereEqualTo("friendUserId", userId)
                .get().await()

            for (doc in friendshipsSnapshot.documents) {
                db.collection("friendships").document(doc.id).update(statusMap).await()
            }
        } catch (e: Exception) {
            Log.w("FriendRepo", "Error sending heartbeat for $userId: ${e.message}")
        }
    }

    suspend fun updateUserStatus(
        userId: String,
        isOnline: Boolean,
        activityStatus: String,
        lastSeenText: String
    ): Result<Unit> {
        if (userId.isBlank()) return Result.success(Unit)
        val path = "users/$userId"
        return try {
            val statusMap = mapOf<String, Any>(
                "isOnline" to isOnline,
                "activityStatus" to activityStatus,
                "lastSeenText" to lastSeenText,
                "lastActive" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(userId).update(statusMap).await()

            val friendshipsSnapshot = db.collection("friendships")
                .whereEqualTo("friendUserId", userId)
                .get().await()

            for (doc in friendshipsSnapshot.documents) {
                db.collection("friendships").document(doc.id).update(statusMap).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteAllGuestData(): Result<Int> {
        return try {
            var deletedCount = 0

            // 1. Delete guest users
            val usersSnapshot = db.collection("users").get().await()
            for (doc in usersSnapshot.documents) {
                val userId = doc.id
                val username = doc.getString("username") ?: ""
                val email = doc.getString("email") ?: ""
                if (userId.startsWith("guest_") || username.lowercase().contains("guest") || email.lowercase().startsWith("guest_")) {
                    db.collection("users").document(doc.id).delete().await()
                    deletedCount++
                }
            }

            // 2. Delete guest friend requests
            val reqsSnapshot = db.collection("friendRequests").get().await()
            for (doc in reqsSnapshot.documents) {
                val reqId = doc.id
                val senderId = doc.getString("senderId") ?: ""
                val recipientId = doc.getString("recipientId") ?: ""
                if (reqId.contains("guest_") || senderId.startsWith("guest_") || recipientId.startsWith("guest_")) {
                    db.collection("friendRequests").document(doc.id).delete().await()
                    deletedCount++
                }
            }

            // 3. Delete guest friendships
            val friendsSnapshot = db.collection("friendships").get().await()
            for (doc in friendsSnapshot.documents) {
                val fId = doc.id
                val u1 = doc.getString("user1Id") ?: ""
                val u2 = doc.getString("user2Id") ?: ""
                val friendUser = doc.getString("friendUserId") ?: ""
                if (fId.contains("guest_") || u1.startsWith("guest_") || u2.startsWith("guest_") || friendUser.startsWith("guest_")) {
                    db.collection("friendships").document(doc.id).delete().await()
                    deletedCount++
                }
            }

            // 4. Delete guest rooms and voice rooms
            val roomsSnapshot = db.collection("rooms").get().await()
            for (doc in roomsSnapshot.documents) {
                val rId = doc.id
                val hostId = doc.getString("hostId") ?: ""
                val hostUserId = doc.getString("hostUserId") ?: ""
                if (rId.startsWith("guest_") || hostId.startsWith("guest_") || hostUserId.startsWith("guest_")) {
                    db.collection("rooms").document(doc.id).delete().await()
                    deletedCount++
                }
            }

            val vRoomsSnapshot = db.collection("voice_rooms").get().await()
            for (doc in vRoomsSnapshot.documents) {
                val rId = doc.id
                val hostId = doc.getString("hostId") ?: ""
                if (rId.startsWith("guest_") || hostId.startsWith("guest_")) {
                    db.collection("voice_rooms").document(doc.id).delete().await()
                    deletedCount++
                }
            }

            Log.i("FriendRepo", "Successfully deleted $deletedCount guest records from Firestore.")
            Result.success(deletedCount)
        } catch (e: Exception) {
            Log.e("FriendRepo", "Error deleting guest data: ${e.message}", e)
            Result.failure(e)
        }
    }
}

