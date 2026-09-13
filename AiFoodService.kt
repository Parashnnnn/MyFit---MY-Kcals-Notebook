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
import kotlin.math.roundToInt

data class NutritionAnalysisResult(
    val foodName: String,
    val estimatedQuantity: String,
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val confidence: String, // "low", "medium", "high"
    val assumptions: List<String>,
    val isAiGenerated: Boolean = true
)

object AiFoodService {
    private const val TAG = "AiFoodService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private const val SYSTEM_PROMPT = """You are a nutrition estimation engine for a food tracking app. You will receive a short, informal description of a meal — often in English, Hindi, or Hinglish, and often referencing Indian home-cooked or restaurant food. Estimate calories and macros for the meal as accurately as possible given the ambiguity.

Rules:
- Interpret informal quantities like "thoda", "half plate", "1 bowl", "2 pieces" using typical serving sizes.
- If quantity is not specified, assume one typical adult serving.
- Break down multi-item meals and sum the totals.
- Recognize common Indian dishes (roti, paratha, dal, rice, rajma, chole, biryani, paneer dishes, chicken/fish curry, samosa, momos, dosa, idli, poha, chai, lassi, etc.) as well as generic/global foods.
- Always return your best estimate — never refuse or ask a clarifying question back. If information is missing, state your assumption in the "assumptions" field instead.
- Respond with ONLY a single JSON object, no other text, matching this exact schema.

Schema:
{
  "food_name": string,
  "estimated_quantity": string,
  "calories": number,
  "protein_g": number,
  "carbs_g": number,
  "fat_g": number,
  "confidence": "low" | "medium" | "high",
  "assumptions": string[]
}"""

    suspend fun analyzeFood(
        description: String,
        mealType: String,
        correctionContext: String? = null
    ): Result<NutritionAnalysisResult> = withContext(Dispatchers.IO) {
        val trimmedDesc = description.trim()
        if (trimmedDesc.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Please describe what you ate."))
        }

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val hasValidApiKey = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        if (hasValidApiKey) {
            try {
                val aiResult = callGeminiApi(apiKey, trimmedDesc, mealType, correctionContext)
                return@withContext Result.success(aiResult)
            } catch (e: Exception) {
                Log.w(TAG, "Gemini API call failed, falling back to smart local nutrition engine: ${e.message}")
            }
        }

        // Offline / fallback smart heuristic engine
        val fallbackResult = estimateLocally(trimmedDesc, mealType, correctionContext)
        Result.success(fallbackResult)
    }

    private fun callGeminiApi(
        apiKey: String,
        description: String,
        mealType: String,
        correctionContext: String?
    ): NutritionAnalysisResult {
        val userPrompt = buildString {
            appendLine("Meal type: ${mealType.lowercase()}")
            appendLine("Description: $description")
            if (!correctionContext.isNullOrBlank()) {
                appendLine("User correction/clarification: $correctionContext")
            }
        }

        val requestJson = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", SYSTEM_PROMPT) })
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", userPrompt) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
                put("responseMimeType", "application/json")
            })
        }

        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw IllegalStateException("Empty response from server")

        if (!response.isSuccessful) {
            throw IllegalStateException("API error (${response.code}): $responseBody")
        }

        val rootJson = JSONObject(responseBody)
        val candidates = rootJson.optJSONArray("candidates")
            ?: throw IllegalStateException("No candidates in response")
        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.getJSONObject("content")
        val parts = content.getJSONArray("parts")
        val rawText = parts.getJSONObject(0).getString("text")

        return parseNutritionJson(rawText)
    }

    private fun parseNutritionJson(rawText: String): NutritionAnalysisResult {
        // Strip code fences if model accidentally emitted ```json ... ```
        val cleaned = rawText
            .replace("```json", "")
            .replace("```", "")
            .trim()

        val json = JSONObject(cleaned)
        val foodName = json.optString("food_name", "Meal")
        val estimatedQuantity = json.optString("estimated_quantity", "1 serving")
        val calories = json.optDouble("calories", 0.0).roundToInt()
        val proteinG = json.optDouble("protein_g", 0.0).roundToInt()
        val carbsG = json.optDouble("carbs_g", 0.0).roundToInt()
        val fatG = json.optDouble("fat_g", 0.0).roundToInt()
        val confidence = json.optString("confidence", "medium")

        val assumptionsList = mutableListOf<String>()
        val assumptionsArray = json.optJSONArray("assumptions")
        if (assumptionsArray != null) {
            for (i in 0 until assumptionsArray.length()) {
                assumptionsList.add(assumptionsArray.getString(i))
            }
        }

        return NutritionAnalysisResult(
            foodName = foodName,
            estimatedQuantity = estimatedQuantity,
            calories = calories,
            proteinG = proteinG,
            carbsG = carbsG,
            fatG = fatG,
            confidence = confidence,
            assumptions = assumptionsList,
            isAiGenerated = true
        )
    }

    /**
     * Comprehensive smart local estimator for Indian and international foods
     * Handles quantities like "2 rotis", "half plate biryani", "1 bowl dal", "thoda rice", etc.
     */
    fun estimateLocally(
        description: String,
        mealType: String,
        correctionContext: String? = null
    ): NutritionAnalysisResult {
        val lower = description.lowercase()

        // Detect items & quantities
        var totalCalories = 0
        var totalProtein = 0
        var totalCarbs = 0
        var totalFat = 0
        val assumptions = mutableListOf<String>()
        val detectedNames = mutableListOf<String>()

        // Roti / Chapati / Phulka
        val rotiMatches = Regex("""(\d+)\s*(roti|rotis|chapati|chapatis|phulka|phulkas)""").find(lower)
        if (rotiMatches != null) {
            val count = rotiMatches.groupValues[1].toIntOrNull() ?: 2
            val cal = count * 90
            val p = count * 3
            val c = count * 18
            val f = count * 1
            totalCalories += cal; totalProtein += p; totalCarbs += c; totalFat += f
            detectedNames.add("$count ${if (count > 1) "Rotis" else "Roti"}")
            assumptions.add("Estimated $count medium home wheat rotis (~90 kcal each)")
        } else if (lower.contains("roti") || lower.contains("chapati")) {
            val count = if (lower.contains("thoda")) 1 else 2
            totalCalories += count * 90; totalProtein += count * 3; totalCarbs += count * 18; totalFat += count * 1
            detectedNames.add("$count Rotis")
            assumptions.add("Assumed standard serving of $count rotis")
        }

        // Paratha
        if (lower.contains("paratha") || lower.contains("parathas")) {
            val count = Regex("""(\d+)\s*paratha""").find(lower)?.groupValues?.get(1)?.toIntOrNull() ?: 1
            val isAloo = lower.contains("aloo")
            val isPaneer = lower.contains("paneer")
            val calPer = if (isPaneer) 280 else if (isAloo) 240 else 200
            val pPer = if (isPaneer) 8 else if (isAloo) 5 else 4
            val cPer = if (isAloo) 32 else 26
            val fPer = if (isPaneer) 13 else 9
            totalCalories += count * calPer; totalProtein += count * pPer; totalCarbs += count * cPer; totalFat += count * fPer
            val typeName = if (isPaneer) "Paneer Paratha" else if (isAloo) "Aloo Paratha" else "Paratha"
            detectedNames.add("$count $typeName")
            assumptions.add("Assumed $count pan-cooked $typeName with moderate ghee/oil")
        }

        // Dal (Yellow dal, tadka, dal makhani)
        if (lower.contains("dal") || lower.contains("daal") || lower.contains("sambhar") || lower.contains("sambar")) {
            val isMakhani = lower.contains("makhani")
            val cal = if (isMakhani) 240 else 150
            val p = if (isMakhani) 7 else 8
            val c = if (isMakhani) 20 else 22
            val f = if (isMakhani) 14 else 4
            totalCalories += cal; totalProtein += p; totalCarbs += c; totalFat += f
            detectedNames.add(if (isMakhani) "Dal Makhani" else "Dal")
            assumptions.add("Assumed 1 medium katori (~150g) of cooked lentils")
        }

        // Chicken Curry / Chicken Biryani
        if (lower.contains("biryani")) {
            val isChicken = lower.contains("chicken") || !lower.contains("veg")
            val isHalf = lower.contains("half") || lower.contains("thoda")
            val mult = if (isHalf) 0.6f else 1.0f
            val cal = (if (isChicken) 550 else 420) * mult
            val p = (if (isChicken) 28 else 12) * mult
            val c = (if (isChicken) 65 else 62) * mult
            val f = (if (isChicken) 18 else 14) * mult
            totalCalories += cal.roundToInt(); totalProtein += p.roundToInt(); totalCarbs += c.roundToInt(); totalFat += f.roundToInt()
            val name = if (isChicken) "Chicken Biryani" else "Vegetable Biryani"
            detectedNames.add(if (isHalf) "Half plate $name" else "1 plate $name")
            assumptions.add("Assumed typical restaurant serving with spiced basmati rice")
        } else if (lower.contains("chicken curry") || lower.contains("chicken")) {
            val cal = 260; val p = 26; val c = 6; val f = 14
            totalCalories += cal; totalProtein += p; totalCarbs += c; totalFat += f
            detectedNames.add("Chicken Curry")
            assumptions.add("Assumed 1 bowl chicken curry with ~150g boneless/bone-in chicken")
        }

        // Rice / Chawal
        if (lower.contains("rice") || lower.contains("chawal") || lower.contains("pulao")) {
            if (!lower.contains("biryani")) {
                val isHalf = lower.contains("half") || lower.contains("thoda")
                val cal = if (isHalf) 130 else 210
                val p = if (isHalf) 3 else 4
                val c = if (isHalf) 28 else 45
                val f = 1
                totalCalories += cal; totalProtein += p; totalCarbs += c; totalFat += f
                detectedNames.add(if (isHalf) "Half bowl Steamed Rice" else "1 bowl Steamed Rice")
                assumptions.add("Assumed plain cooked white basmati rice")
            }
        }

        // Paneer
        if (lower.contains("paneer") && !lower.contains("paratha")) {
            val cal = 300; val p = 14; val c = 10; val f = 22
            totalCalories += cal; totalProtein += p; totalCarbs += c; totalFat += f
            detectedNames.add("Paneer Sabzi")
            assumptions.add("Assumed 1 cup paneer gravy dish with ~75g fresh paneer")
        }

        // Rajma / Chole
        if (lower.contains("rajma") || lower.contains("chole") || lower.contains("chana")) {
            val cal = 210; val p = 9; val c = 32; val f = 5
            totalCalories += cal; totalProtein += p; totalCarbs += c; totalFat += f
            val name = if (lower.contains("rajma")) "Rajma Curry" else "Chole Masala"
            detectedNames.add(name)
            assumptions.add("Assumed 1 bowl cooked spiced legumes")
        }

        // Eggs / Omelette
        val eggMatches = Regex("""(\d+)\s*(egg|eggs|anda|ande)""").find(lower)
        if (eggMatches != null || lower.contains("egg") || lower.contains("omelette") || lower.contains("omlet")) {
            val count = eggMatches?.groupValues?.get(1)?.toIntOrNull() ?: 2
            val isBoiled = lower.contains("boiled")
            val calPer = if (isBoiled) 75 else 95
            val pPer = 6
            val fPer = if (isBoiled) 5 else 7
            totalCalories += count * calPer; totalProtein += count * pPer; totalCarbs += count * 1; totalFat += count * fPer
            val eggDesc = if (lower.contains("omelette")) "$count Egg Omelette" else "$count Eggs"
            detectedNames.add(eggDesc)
            assumptions.add("Assumed $count large whole eggs")
        }

        // Bread / Toast
        val breadMatches = Regex("""(\d+)\s*(slice|slices|bread|breads|toast)""").find(lower)
        if (breadMatches != null || lower.contains("bread") || lower.contains("toast")) {
            val count = breadMatches?.groupValues?.get(1)?.toIntOrNull() ?: 2
            totalCalories += count * 75; totalProtein += count * 3; totalCarbs += count * 14; totalFat += count * 1
            detectedNames.add("$count Slices Bread")
            assumptions.add("Assumed standard white/wheat sliced bread")
        }

        // South Indian: Dosa, Idli
        if (lower.contains("dosa") || lower.contains("masala dosa")) {
            val count = Regex("""(\d+)\s*dosa""").find(lower)?.groupValues?.get(1)?.toIntOrNull() ?: 1
            val isMasala = lower.contains("masala")
            val cal = count * (if (isMasala) 280 else 180)
            val p = count * (if (isMasala) 6 else 4)
            val c = count * (if (isMasala) 42 else 30)
            val f = count * (if (isMasala) 10 else 5)
            totalCalories += cal; totalProtein += p; totalCarbs += c; totalFat += f
            detectedNames.add("$count ${if (isMasala) "Masala Dosa" else "Plain Dosa"}")
            assumptions.add("Assumed medium crisp dosa with chutney & sambar accompaniment")
        }
        if (lower.contains("idli") || lower.contains("idlis")) {
            val count = Regex("""(\d+)\s*idli""").find(lower)?.groupValues?.get(1)?.toIntOrNull() ?: 2
            totalCalories += count * 60; totalProtein += count * 2; totalCarbs += count * 12; totalFat += 1
            detectedNames.add("$count Idlis")
            assumptions.add("Assumed steamed rice-urad dal idlis")
        }

        // Poha / Upma
        if (lower.contains("poha") || lower.contains("upma")) {
            totalCalories += 220; totalProtein += 4; totalCarbs += 38; totalFat += 6
            detectedNames.add(if (lower.contains("poha")) "Poha" else "Upma")
            assumptions.add("Assumed 1 standard breakfast bowl with peanuts/vegetables")
        }

        // Chai / Tea / Coffee
        if (lower.contains("chai") || lower.contains("tea") || lower.contains("coffee")) {
            val count = Regex("""(\d+)\s*(cup|cups)""").find(lower)?.groupValues?.get(1)?.toIntOrNull() ?: 1
            val cal = count * 90; val p = count * 3; val c = count * 12; val f = count * 3
            totalCalories += cal; totalProtein += p; totalCarbs += c; totalFat += f
            detectedNames.add(if (lower.contains("coffee")) "Coffee" else "Chai")
            assumptions.add("Assumed whole milk and 1 tsp sugar")
        }

        // Samosa / Momos / Snacks
        if (lower.contains("samosa") || lower.contains("samosas")) {
            val count = Regex("""(\d+)\s*samosa""").find(lower)?.groupValues?.get(1)?.toIntOrNull() ?: 1
            totalCalories += count * 250; totalProtein += count * 4; totalCarbs += count * 28; totalFat += count * 14
            detectedNames.add("$count Samosa")
            assumptions.add("Assumed fried potato-stuffed samosa")
        }
        if (lower.contains("momo") || lower.contains("momos")) {
            val count = Regex("""(\d+)\s*momo""").find(lower)?.groupValues?.get(1)?.toIntOrNull() ?: 6
            totalCalories += count * 45; totalProtein += count * 3; totalCarbs += count * 6; totalFat += count * 1
            detectedNames.add("$count Momos")
            assumptions.add("Assumed steamed dumplings")
        }

        // Oats / Milk / Fruit
        if (lower.contains("oats") || lower.contains("oatmeal")) {
            totalCalories += 220; totalProtein += 8; totalCarbs += 36; totalFat += 5
            detectedNames.add("Oatmeal")
            assumptions.add("Assumed 1 bowl cooked oats with milk")
        }
        if (lower.contains("banana") || lower.contains("kela")) {
            totalCalories += 105; totalProtein += 1; totalCarbs += 27; totalFat += 0
            detectedNames.add("1 Banana")
            assumptions.add("Assumed 1 medium ripe banana (~118g)")
        }
        if (lower.contains("apple") || lower.contains("seb")) {
            totalCalories += 80; totalProtein += 0; totalCarbs += 20; totalFat += 0
            detectedNames.add("1 Apple")
            assumptions.add("Assumed 1 medium fresh apple")
        }

        // Generic fallback if nothing matched
        if (totalCalories == 0) {
            val defaultCal = when (mealType.lowercase()) {
                "breakfast" -> 350
                "lunch" -> 500
                "dinner" -> 480
                else -> 220
            }
            totalCalories = defaultCal
            totalProtein = (defaultCal * 0.20 / 4).roundToInt()
            totalCarbs = (defaultCal * 0.50 / 4).roundToInt()
            totalFat = (defaultCal * 0.30 / 9).roundToInt()
            detectedNames.add(description.take(30).replaceFirstChar { it.uppercase() })
            assumptions.add("Estimated standard serving based on typical $mealType portions")
        }

        val finalName = if (detectedNames.isNotEmpty()) detectedNames.joinToString(", ") else description
        val finalQuantity = when {
            detectedNames.size > 1 -> "Combined plate (${detectedNames.size} items)"
            detectedNames.size == 1 -> detectedNames.first()
            else -> "1 serving"
        }

        return NutritionAnalysisResult(
            foodName = finalName,
            estimatedQuantity = finalQuantity,
            calories = totalCalories,
            proteinG = totalProtein,
            carbsG = totalCarbs,
            fatG = totalFat,
            confidence = if (assumptions.size > 1) "medium" else "high",
            assumptions = assumptions,
            isAiGenerated = false
        )
    }
}
