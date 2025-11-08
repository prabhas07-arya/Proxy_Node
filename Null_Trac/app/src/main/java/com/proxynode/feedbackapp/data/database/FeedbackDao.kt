package com.proxynode.feedbackapp.data.database

import androidx.room.*
import com.proxynode.feedbackapp.data.model.FeedbackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedbackDao {

    @Query("SELECT * FROM feedback ORDER BY timestamp DESC")
    fun getAllFeedback(): Flow<List<FeedbackEntity>>

    @Query("SELECT * FROM feedback WHERE deviceId = :deviceId ORDER BY timestamp DESC")
    fun getFeedbackByDevice(deviceId: String): Flow<List<FeedbackEntity>>

    @Query("SELECT COUNT(*) FROM feedback")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM feedback WHERE tag = :tag")
    suspend fun getCountByTag(tag: String): Int

    @Query("SELECT * FROM feedback WHERE tag = :tag ORDER BY timestamp DESC")
    fun getFeedbackByTag(tag: String): Flow<List<FeedbackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: FeedbackEntity): Long

    @Delete
    suspend fun deleteFeedback(feedback: FeedbackEntity)

    @Query("DELETE FROM feedback")
    suspend fun deleteAllFeedback()
}