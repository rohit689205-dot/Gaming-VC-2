package com.example.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.Timestamp

@IgnoreExtraProperties
data class FriendRequest(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderUsername: String = "",
    val senderAvatar: String = "",
    val recipientId: String = "",
    val recipientName: String = "",
    val recipientUsername: String = "",
    val recipientAvatar: String = "",
    val status: String = "pending", // "pending", "accepted", "declined"
    @ServerTimestamp val createdAt: Timestamp? = null
)
