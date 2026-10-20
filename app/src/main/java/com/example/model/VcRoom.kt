package com.example.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.Timestamp

@IgnoreExtraProperties
data class VcRoom(
    @DocumentId val id: String = "",
    val roomId: String = "",
    val roomName: String = "",
    val hostUserId: String = "",
    val hostName: String = "",
    val inviteCode: String = "",
    val privacyMode: String = "link",
    val status: String = "active",
    val participantCount: Int = 1,
    @ServerTimestamp val createdAt: Timestamp? = null,
    val endedAt: Timestamp? = null
)
