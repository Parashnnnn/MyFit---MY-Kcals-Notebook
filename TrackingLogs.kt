package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey val date: String, // format YYYY-MM-DD
    val mlConsumed: Int = 0
)

@Entity(tableName = "weight_logs")
data class WeightLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // format YYYY-MM-DD
    val weightKg: Float,
    val timestamp: Long = System.currentTimeMillis()
)
