package com.proxynode.feedbackapp.data.repository

import android.content.Context
import android.provider.Settings
import com.proxynode.feedbackapp.data.ai.FeedbackAnalyzer
import com.proxynode.feedbackapp.data.database.FeedbackDatabase
import com.proxynode.feedbackapp.data.model.FeedbackEntity
import com.proxynode.feedbackapp.data.model.FeedbackResult
import com.proxynode.feedbackapp.data.model.FeedbackStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Date

class FeedbackRepository(private val context: Context) {

    private val database = FeedbackDatabase.getDatabase(context)
    private val feedbackDao = database.feedbackDao()
    private val feedbackAnalyzer = FeedbackAnalyzer()

    private val deviceId: String by lazy {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown_device"
    }

    suspend fun initializeAI(): Boolean {
        return feedbackAnalyzer.initializeModel()
    }

    suspend fun submitFeedback(text: String, isVoiceInput: Boolean = false): Result<Long> {
        return try {
            // Analyze feedback using AI
            val analysisResult = feedbackAnalyzer.analyzeFeedback(text)

            // Create feedback entity
            val feedbackEntity = FeedbackEntity(
                originalText = text,
                anonymizedText = analysisResult.anonymizedText,
                summary = analysisResult.summary,
                tag = analysisResult.tag,
                isVoiceInput = isVoiceInput,
                timestamp = Date(),
                deviceId = deviceId
            )

            // Save to database
            val id = feedbackDao.insertFeedback(feedbackEntity)
            Result.success(id)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserFeedback(): Flow<List<FeedbackEntity>> {
        return feedbackDao.getFeedbackByDevice(deviceId)
    }

    fun getAllFeedback(): Flow<List<FeedbackEntity>> {
        return feedbackDao.getAllFeedback()
    }

    fun getFeedbackByTag(tag: String): Flow<List<FeedbackEntity>> {
        return feedbackDao.getFeedbackByTag(tag)
    }

    suspend fun getFeedbackStats(): FeedbackStats {
        val total = feedbackDao.getTotalCount()
        val academics = feedbackDao.getCountByTag("Academics")
        val infrastructure = feedbackDao.getCountByTag("Infrastructure")
        val placement = feedbackDao.getCountByTag("Placement")
        val other = feedbackDao.getCountByTag("Other")

        return FeedbackStats(
            totalFeedback = total,
            academicsCount = academics,
            infrastructureCount = infrastructure,
            placementCount = placement,
            otherCount = other
        )
    }

    suspend fun generateTestData(): Result<Unit> {
        return try {
            val testFeedbacks = listOf(
                "The teaching quality in computer science department needs improvement" to false,
                "WiFi connectivity in the library is very poor and affects our studies" to false,
                "More companies should visit our campus for placement opportunities" to false,
                "The lab equipment is outdated and needs immediate replacement" to false,
                "Faculty should provide more practical examples during lectures" to false,
                "Hostel food quality is below average and unhygienic" to false,
                "Career counseling sessions are not frequent enough" to false,
                "Air conditioning in classrooms doesn't work properly" to false
            )

            testFeedbacks.forEach { (text, isVoice) ->
                submitFeedback(text, isVoice)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllFeedback() {
        feedbackDao.deleteAllFeedback()
    }
}