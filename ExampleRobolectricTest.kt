package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.AiFoodService
import com.example.util.BmrCalculator
import com.example.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MyFit", appName)
    }

    @Test
    fun `calculate BMR and TDEE for male`() {
        // Mifflin-St Jeor for 25 yo male, 172 cm, 70 kg
        // BMR = 10*70 + 6.25*172 - 5*25 + 5 = 700 + 1075 - 125 + 5 = 1655
        val result = BmrCalculator.calculate(
            age = 25,
            heightCm = 172f,
            weightKg = 70f,
            sex = "male",
            activityLevel = "moderate",
            goal = "lose",
            goalRate = "moderate"
        )

        assertEquals(1655, result.bmr)
        // TDEE = 1655 * 1.55 = 2565
        assertEquals(2565, result.tdee)
        // Target = 2565 - 500 = 2065
        assertEquals(2065, result.calorieTarget)
        assertTrue(result.proteinG > 0)
        assertTrue(result.carbsG > 0)
        assertTrue(result.fatG > 0)
    }

    @Test
    fun `safe minimum threshold enforced for female`() {
        // Very low weight/calories request should not dip below 1200
        val result = BmrCalculator.calculate(
            age = 40,
            heightCm = 150f,
            weightKg = 40f,
            sex = "female",
            activityLevel = "sedentary",
            goal = "lose",
            goalRate = "aggressive"
        )
        assertTrue(result.calorieTarget >= 1200)
    }

    @Test
    fun `local food estimator recognizes Indian meals`() {
        val result = AiFoodService.estimateLocally("2 rotis and 1 bowl dal", "Lunch")
        assertTrue(result.calories > 0)
        assertTrue(result.proteinG > 0)
        assertTrue(result.foodName.contains("Roti", ignoreCase = true))
        assertTrue(result.foodName.contains("Dal", ignoreCase = true))
    }

    @Test
    fun `streak calculation handles consecutive days`() {
        val today = LocalDate.now()
        val dates = listOf(
            DateUtils.formatDateToString(today),
            DateUtils.formatDateToString(today.minusDays(1)),
            DateUtils.formatDateToString(today.minusDays(2))
        )
        val streak = DateUtils.calculateStreak(dates)
        assertEquals(3, streak)
    }
}
