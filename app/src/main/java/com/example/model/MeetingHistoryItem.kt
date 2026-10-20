package com.example.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.Timestamp

@IgnoreExtraProperties
data class MeetingHistoryItem(
    @DocumentId val id: String = "",
    val roomId: String = "",
    val roomName: String = "",
    val hostUserId: String = "",
    val hostName: String = "",
    val inviteCode: String = "",
    val durationMinutes: Int = 0,
    val status: String = "Completed",
    val participantCount: Int = 1,
    @ServerTimestamp val joinedAt: Timestamp? = null,
    val leftAt: Timestamp? = null
)
