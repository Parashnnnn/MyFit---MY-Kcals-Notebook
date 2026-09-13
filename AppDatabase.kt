package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.FoodEntryDao
import com.example.data.dao.UserProfileDao
import com.example.data.dao.WaterLogDao
import com.example.data.dao.WeightLogDao
import com.example.data.model.FoodEntry
import com.example.data.model.UserProfile
import com.example.data.model.WaterLog
import com.example.data.model.WeightLog

@Database(
    entities = [
        UserProfile::class,
        FoodEntry::class,
        WaterLog::class,
        WeightLog::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun foodEntryDao(): FoodEntryDao
    abstract fun waterLogDao(): WaterLogDao
    abstract fun weightLogDao(): WeightLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myfit_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
