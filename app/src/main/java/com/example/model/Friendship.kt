package com.example.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.Timestamp

@IgnoreExtraProperties
data class Friendship(
    @DocumentId val id: String = "",
    val user1Id: String = "",
    val user2Id: String = "",
    val friendUserId: String = "",
    val friendDisplayName: String = "",
    val friendUsername: String = "",
    val friendAvatarUrl: String = "",
    val isOnline: Boolean = true,
    val activityStatus: String = "Playing Valorant",
    val lastSeenText: String = "Just now",
    @ServerTimestamp val lastActive: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)
