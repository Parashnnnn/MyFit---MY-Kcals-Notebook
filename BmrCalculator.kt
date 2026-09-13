package com.example.util

import kotlin.math.max
import kotlin.math.roundToInt

data class CalculatedNutritionTarget(
    val bmr: Int,
    val tdee: Int,
    val calorieTarget: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val isCappedAtMinimum: Boolean
)

object BmrCalculator {
    fun calculate(
        age: Int,
        heightCm: Float,
        weightKg: Float,
        sex: String, // "male" or "female"
        activityLevel: String, // "sedentary", "light", "moderate", "active", "very_active"
        goal: String, // "lose", "maintain", "gain"
        goalRate: String // "mild", "moderate", "aggressive"
    ): CalculatedNutritionTarget {
        // Mifflin-St Jeor Equation
        val baseBmr = if (sex.lowercase() == "female") {
            (10f * weightKg) + (6.25f * heightCm) - (5f * age) - 161f
        } else {
            (10f * weightKg) + (6.25f * heightCm) - (5f * age) + 5f
        }
        val bmr = max(900, baseBmr.roundToInt())

        val activityMultiplier = when (activityLevel.lowercase()) {
            "sedentary" -> 1.20f
            "light" -> 1.375f
            "moderate" -> 1.55f
            "active" -> 1.725f
            "very_active" -> 1.90f
            else -> 1.40f
        }

        val tdee = (bmr * activityMultiplier).roundToInt()

        val adjustment = when (goal.lowercase()) {
            "lose" -> when (goalRate.lowercase()) {
                "mild" -> -250
                "aggressive" -> -750
                else -> -500 // moderate
            }
            "gain" -> when (goalRate.lowercase()) {
                "mild" -> 250
                "aggressive" -> 750
                else -> 500 // moderate
            }
            else -> 0 // maintain
        }

        val rawCalorieTarget = tdee + adjustment

        // Safe minimums: 1200 for women, 1500 for men
        val safeMinimum = if (sex.lowercase() == "female") 1200 else 1500
        val isCapped = rawCalorieTarget < safeMinimum
        val finalCalorieTarget = max(safeMinimum, rawCalorieTarget)

        // Default macro split: 30% protein, 40% carbs, 30% fat
        val proteinG = ((finalCalorieTarget * 0.30f) / 4f).roundToInt()
        val carbsG = ((finalCalorieTarget * 0.40f) / 4f).roundToInt()
        val fatG = ((finalCalorieTarget * 0.30f) / 9f).roundToInt()

        return CalculatedNutritionTarget(
            bmr = bmr,
            tdee = tdee,
            calorieTarget = finalCalorieTarget,
            proteinG = proteinG,
            carbsG = carbsG,
            fatG = fatG,
            isCappedAtMinimum = isCapped
        )
    }

    // Helper conversion utilities
    fun ftInToCm(feet: Int, inches: Int): Float {
        val totalInches = (feet * 12) + inches
        return totalInches * 2.54f
    }

    fun cmToFtIn(cm: Float): Pair<Int, Int> {
        val totalInches = (cm / 2.54f).roundToInt()
        val feet = totalInches / 12
        val inches = totalInches % 12
        return Pair(feet, inches)
    }

    fun lbsToKg(lbs: Float): Float = lbs * 0.45359237f
    fun kgToLbs(kg: Float): Float = kg * 2.20462f
}
