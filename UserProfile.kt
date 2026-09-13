package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val age: Int = 26,
    val heightCm: Float = 172f,
    val isMetricHeight: Boolean = true,
    val weightKg: Float = 70f,
    val isMetricWeight: Boolean = true,
    val sex: String = "male", // "male" or "female"
    val activityLevel: String = "moderate", // "sedentary", "light", "moderate", "active", "very_active"
    val goal: String = "lose", // "lose", "maintain", "gain"
    val goalRate: String = "moderate", // "mild", "moderate", "aggressive"
    val bmr: Int = 1660,
    val tdee: Int = 2300,
    val calorieTarget: Int = 1800,
    val proteinTargetG: Int = 135,
    val carbsTargetG: Int = 180,
    val fatTargetG: Int = 60,
    val waterGoalMl: Int = 2500,
    val goalWeightKg: Float = 68f,
    val themeMode: String = "light", // "light" or "dark"
    val isOnboarded: Boolean = false
)
