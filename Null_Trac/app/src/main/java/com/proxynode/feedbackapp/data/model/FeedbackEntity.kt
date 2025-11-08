package com.proxynode.feedbackapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "feedback")
data class FeedbackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val originalText: String,
    val anonymizedText: String,
    val summary: String,
    val tag: String,
    val isVoiceInput: Boolean = false,
    val timestamp: Date = Date(),
    val deviceId: String // Simple device identifier for privacy
)