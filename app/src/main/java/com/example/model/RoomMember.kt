package com.example.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.Timestamp

@IgnoreExtraProperties
data class RoomMember(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val isMuted: Boolean = false,
    val isSpeaking: Boolean = false,
    @ServerTimestamp val joinedAt: Timestamp? = null
)
