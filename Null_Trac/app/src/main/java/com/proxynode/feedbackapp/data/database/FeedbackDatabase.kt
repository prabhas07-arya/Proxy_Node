package com.proxynode.feedbackapp.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.proxynode.feedbackapp.data.model.FeedbackEntity

@Database(
    entities = [FeedbackEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class FeedbackDatabase : RoomDatabase() {

    abstract fun feedbackDao(): FeedbackDao

    companion object {
        @Volatile
        private var INSTANCE: FeedbackDatabase? = null

        fun getDatabase(context: Context): FeedbackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context)
                INSTANCE = instance
                instance
            }
        }

        private fun buildDatabase(context: Context): FeedbackDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                FeedbackDatabase::class.java,
                "feedback_database"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}