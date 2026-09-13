package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class MealSuggestion(
    val title: String,
    val description: String,
    val estimatedCalories: Int,
    val estimatedProteinG: Int
)

object MealSuggestionService {
    private const val TAG = "MealSuggestionService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    suspend fun getSuggestions(remainingCalories: Int): List<MealSuggestion> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    Suggest 3 quick, practical meal or snack ideas for someone who has approximately $remainingCalories calories remaining in their daily target.
                    Focus on healthy, satisfying, high-protein options (including common Indian home items like paneer, curd/chaas, boiled eggs, dal soup, sprout salad, roasted chana, chicken tikka, etc.).
                    Always phrase suggestions as ideas, never as medical or dietary advice.
                    Respond ONLY with a JSON array of objects with keys: "title", "description", "calories", "protein_g".
                """.trimIndent()

                val requestJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", prompt) })
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("responseMimeType", "application/json")
                        put("temperature", 0.3)
                    })
                }

                val request = Request.Builder()
                    .url("$BASE_URL?key=$apiKey")
                    .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful && responseBody.isNotEmpty()) {
                    val root = JSONObject(responseBody)
                    val text = root.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")

                    val cleaned = text.replace("```json", "").replace("```", "").trim()
                    val array = JSONArray(cleaned)
                    val list = mutableListOf<MealSuggestion>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        list.add(
                            MealSuggestion(
                                title = obj.optString("title", "Meal Idea"),
                                description = obj.optString("description", ""),
                                estimatedCalories = obj.optInt("calories", 250),
                                estimatedProteinG = obj.optInt("protein_g", 15)
                            )
                        )
                    }
                    if (list.isNotEmpty()) return@withContext list
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini suggestions failed: ${e.message}")
            }
        }

        // Smart curated options tailored to remaining calorie budget
        getFallbackSuggestions(remainingCalories)
    }

    private fun getFallbackSuggestions(remainingCalories: Int): List<MealSuggestion> {
        return when {
            remainingCalories <= 250 -> listOf(
                MealSuggestion(
                    title = "Sprout Chaat & Boiled Egg",
                    description = "A refreshing bowl of boiled moong sprouts with onion, tomato, lemon juice and 1 boiled egg.",
                    estimatedCalories = 180,
                    estimatedProteinG = 12
                ),
                MealSuggestion(
                    title = "Greek Yogurt or Hung Curd with Berries",
                    description = "150g plain curd or Greek yogurt sprinkled with a pinch of roasted cumin or fresh berries.",
                    estimatedCalories = 130,
                    estimatedProteinG = 14
                ),
                MealSuggestion(
                    title = "Roasted Chana & Salted Chaas",
                    description = "A handful of crunchy roasted chickpeas paired with chilled spiced buttermilk.",
                    estimatedCalories = 190,
                    estimatedProteinG = 9
                )
            )
            remainingCalories <= 500 -> listOf(
                MealSuggestion(
                    title = "Paneer Tikka / Sautéed Paneer Bowl",
                    description = "100g pan-seared paneer cubes tossed with bell peppers, chaat masala and mint chutney.",
                    estimatedCalories = 320,
                    estimatedProteinG = 18
                ),
                MealSuggestion(
                    title = "Egg Bhurji with 1 Whole Wheat Roti",
                    description = "2-egg spiced scramble with chopped onion, green chillies, coriander and 1 soft chapati.",
                    estimatedCalories = 310,
                    estimatedProteinG = 16
                ),
                MealSuggestion(
                    title = "Grilled Chicken Breast with Steamed Veggies",
                    description = "150g herb-marinated chicken breast served with steamed broccoli, carrots, and lemon.",
                    estimatedCalories = 290,
                    estimatedProteinG = 34
                )
            )
            else -> listOf(
                MealSuggestion(
                    title = "Dal Tadka with 2 Rotis & Cucumber Salad",
                    description = "Warm yellow lentil soup with cumin-garlic tempering, two whole-wheat rotis and fresh salad.",
                    estimatedCalories = 480,
                    estimatedProteinG = 20
                ),
                MealSuggestion(
                    title = "Chicken Curry with Steamed Rice",
                    description = "Home-style chicken curry with 1 small bowl of steamed basmati rice and onion slices.",
                    estimatedCalories = 540,
                    estimatedProteinG = 32
                ),
                MealSuggestion(
                    title = "Tofu/Paneer Stir-Fry with Quinoa or Brown Rice",
                    description = "Crispy pan-fried paneer or tofu with colorful crunchy veggies and a light soy-chilli glaze.",
                    estimatedCalories = 460,
                    estimatedProteinG = 22
                )
            )
        }
    }
}
