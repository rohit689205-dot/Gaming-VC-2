package com.example.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.Timestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val username: String = "",
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val isOnline: Boolean = true,
    val activityStatus: String = "Playing Valorant",
    val lastSeenText: String = "Just now",
    @ServerTimestamp val lastActive: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)
