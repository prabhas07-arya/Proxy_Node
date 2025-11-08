package com.proxynode.feedbackapp.data.model

data class FeedbackResult(
    val originalText: String,
    val anonymizedText: String,
    val summary: String,
    val tag: String
)

enum class FeedbackTag(val displayName: String) {
    ACADEMICS("Academics"),
    INFRASTRUCTURE("Infrastructure"),
    PLACEMENT("Placement"),
    OTHER("Other")
}

data class FeedbackStats(
    val totalFeedback: Int,
    val academicsCount: Int,
    val infrastructureCount: Int,
    val placementCount: Int,
    val otherCount: Int
)