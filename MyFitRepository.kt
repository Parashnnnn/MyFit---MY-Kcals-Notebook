package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.model.FoodEntry
import com.example.data.model.UserProfile
import com.example.data.model.WaterLog
import com.example.data.model.WeightLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MyFitRepository(private val db: AppDatabase) {
    val userProfile: Flow<UserProfile?> = db.userProfileDao().getUserProfile()

    suspend fun getUserProfileOnce(): UserProfile? = db.userProfileDao().getUserProfileOnce()

    suspend fun saveUserProfile(profile: UserProfile) {
        db.userProfileDao().insertOrUpdate(profile)
    }

    suspend fun clearUserProfile() {
        db.userProfileDao().clearProfile()
    }

    fun getEntriesForDate(date: String): Flow<List<FoodEntry>> {
        return db.foodEntryDao().getEntriesForDate(date)
    }

    fun getAllEntries(): Flow<List<FoodEntry>> {
        return db.foodEntryDao().getAllEntries()
    }

    fun getRecentEntries(limit: Int = 4): Flow<List<FoodEntry>> {
        return db.foodEntryDao().getRecentEntries(limit)
    }

    fun searchEntries(query: String): Flow<List<FoodEntry>> {
        return db.foodEntryDao().searchEntries(query)
    }

    fun getEntriesInRange(startDate: String, endDate: String): Flow<List<FoodEntry>> {
        return db.foodEntryDao().getEntriesInRange(startDate, endDate)
    }

    fun getDistinctLoggedDates(): Flow<List<String>> {
        return db.foodEntryDao().getDistinctLoggedDates()
    }

    suspend fun insertFoodEntry(entry: FoodEntry): Long {
        return db.foodEntryDao().insert(entry)
    }

    suspend fun updateFoodEntry(entry: FoodEntry) {
        db.foodEntryDao().update(entry)
    }

    suspend fun deleteFoodEntry(id: Long) {
        db.foodEntryDao().deleteById(id)
    }

    fun getWaterForDate(date: String): Flow<WaterLog?> {
        return db.waterLogDao().getWaterForDate(date)
    }

    suspend fun saveWaterLog(date: String, ml: Int) {
        db.waterLogDao().insertOrUpdate(WaterLog(date = date, mlConsumed = ml))
    }

    fun getAllWeightLogs(): Flow<List<WeightLog>> {
        return db.weightLogDao().getAllWeightLogs()
    }

    suspend fun insertWeightLog(date: String, weightKg: Float) {
        db.weightLogDao().insert(WeightLog(date = date, weightKg = weightKg))
    }
}
