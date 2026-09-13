package com.example.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateUtils {
    private val ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val FRIENDLY_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)
    private val MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("MMM d", Locale.US)

    fun getTodayDate(): LocalDate = LocalDate.now()

    fun getTodayDateString(): String = getTodayDate().format(ISO_DATE_FORMATTER)

    fun formatDateToString(date: LocalDate): String = date.format(ISO_DATE_FORMATTER)

    fun parseDate(dateStr: String): LocalDate {
        return try {
            LocalDate.parse(dateStr, ISO_DATE_FORMATTER)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    fun formatDisplayDate(dateStr: String): String {
        val date = parseDate(dateStr)
        val today = LocalDate.now()
        return when (ChronoUnit.DAYS.between(date, today)) {
            0L -> "Today"
            1L -> "Yesterday"
            else -> date.format(FRIENDLY_FORMATTER)
        }
    }

    fun formatShortDate(dateStr: String): String {
        val date = parseDate(dateStr)
        return date.format(MONTH_DAY_FORMATTER)
    }

    fun suggestMealTypeForCurrentTime(): String {
        val hour = LocalTime.now().hour
        return when (hour) {
            in 5..10 -> "Breakfast"
            in 11..15 -> "Lunch"
            in 16..18 -> "Snack"
            else -> "Dinner"
        }
    }

    fun getGreetingForCurrentTime(name: String): String {
        val hour = LocalTime.now().hour
        val greeting = when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
        val displayName = name.ifBlank { "there" }
        return "$greeting, $displayName 👋"
    }

    fun calculateStreak(distinctDates: List<String>): Int {
        if (distinctDates.isEmpty()) return 0
        val sortedDates = distinctDates
            .mapNotNull { runCatching { LocalDate.parse(it, ISO_DATE_FORMATTER) }.getOrNull() }
            .distinct()
            .sortedDescending()

        if (sortedDates.isEmpty()) return 0

        val today = LocalDate.now()
        val mostRecent = sortedDates.first()
        val daysDiff = ChronoUnit.DAYS.between(mostRecent, today)

        // If user logged today or yesterday, streak is alive
        if (daysDiff > 1) return 0

        var streak = 1
        var expectedDate = mostRecent.minusDays(1)

        for (i in 1 until sortedDates.size) {
            val date = sortedDates[i]
            if (date == expectedDate) {
                streak++
                expectedDate = expectedDate.minusDays(1)
            } else if (date.isBefore(expectedDate)) {
                break
            }
        }
        return streak
    }
}
