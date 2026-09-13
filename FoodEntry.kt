package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_entries")
data class FoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // format YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val mealType: String, // "Breakfast", "Lunch", "Snack", "Dinner"
    val foodName: String,
    val rawDescription: String,
    val estimatedQuantity: String,
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val confidence: String = "medium", // "low", "medium", "high"
    val assumptions: String = ""
)
