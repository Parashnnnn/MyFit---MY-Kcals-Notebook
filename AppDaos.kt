package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FoodEntry
import com.example.data.model.UserProfile
import com.example.data.model.WaterLog
import com.example.data.model.WeightLog
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfile)

    @Query("DELETE FROM user_profile")
    suspend fun clearProfile()
}

@Dao
interface FoodEntryDao {
    @Query("SELECT * FROM food_entries WHERE date = :date ORDER BY timestamp DESC")
    fun getEntriesForDate(date: String): Flow<List<FoodEntry>>

    @Query("SELECT * FROM food_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<FoodEntry>>

    @Query("SELECT * FROM food_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEntries(limit: Int = 5): Flow<List<FoodEntry>>

    @Query("SELECT * FROM food_entries WHERE foodName LIKE '%' || :query || '%' OR rawDescription LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchEntries(query: String): Flow<List<FoodEntry>>

    @Query("SELECT * FROM food_entries WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC, timestamp ASC")
    fun getEntriesInRange(startDate: String, endDate: String): Flow<List<FoodEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: FoodEntry): Long

    @Update
    suspend fun update(entry: FoodEntry)

    @Query("DELETE FROM food_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT DISTINCT date FROM food_entries ORDER BY date DESC")
    fun getDistinctLoggedDates(): Flow<List<String>>
}

@Dao
interface WaterLogDao {
    @Query("SELECT * FROM water_logs WHERE date = :date LIMIT 1")
    fun getWaterForDate(date: String): Flow<WaterLog?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(log: WaterLog)
}

@Dao
interface WeightLogDao {
    @Query("SELECT * FROM weight_logs ORDER BY date DESC, timestamp DESC")
    fun getAllWeightLogs(): Flow<List<WeightLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: WeightLog)
}
