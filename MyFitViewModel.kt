package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ai.AiFoodService
import com.example.data.ai.MealSuggestion
import com.example.data.ai.MealSuggestionService
import com.example.data.ai.NutritionAnalysisResult
import com.example.data.model.FoodEntry
import com.example.data.model.UserProfile
import com.example.data.repository.MyFitRepository
import com.example.util.BmrCalculator
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class NavigationTab(val label: String) {
    HOME("Home"),
    ADD_FOOD("Add Food"),
    CALORIES("Calories"),
    HISTORY("History")
}

sealed interface AnalysisUiState {
    object Idle : AnalysisUiState
    object Loading : AnalysisUiState
    data class Success(val result: NutritionAnalysisResult) : AnalysisUiState
    data class Error(val message: String) : AnalysisUiState
}

data class DailyCalorieSummary(
    val date: String,
    val dayLabel: String,
    val totalCalories: Int,
    val totalProteinG: Int,
    val totalCarbsG: Int,
    val totalFatG: Int
)

class MyFitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MyFitRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = MyFitRepository(db)
    }

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _currentTab = MutableStateFlow(NavigationTab.HOME)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _selectedDate = MutableStateFlow(DateUtils.getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val todayEntries: StateFlow<List<FoodEntry>> = repository.getEntriesForDate(DateUtils.getTodayDateString())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedDateEntries: StateFlow<List<FoodEntry>> = _selectedDate
        .flatMapLatest { date -> repository.getEntriesForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHistoryEntries: StateFlow<List<FoodEntry>> = repository.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentEntries: StateFlow<List<FoodEntry>> = repository.getRecentEntries(4)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayWaterLog = repository.getWaterForDate(DateUtils.getTodayDateString())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allWeightLogs = repository.getAllWeightLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val streakCount: StateFlow<Int> = repository.getDistinctLoggedDates()
        .map { dates -> DateUtils.calculateStreak(dates) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Past 7 days summaries for Weekly Bar Chart and Insights
    val weeklyDailySummaries: StateFlow<List<DailyCalorieSummary>> = repository.getAllEntries()
        .map { entries ->
            val today = LocalDate.now()
            val dayFormatter = DateTimeFormatter.ofPattern("EEE")
            val isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            (6 downTo 0).map { daysAgo ->
                val targetDate = today.minusDays(daysAgo.toLong())
                val dateStr = targetDate.format(isoFormatter)
                val dayName = if (daysAgo == 0) "Today" else targetDate.format(dayFormatter)

                val dayEntries = entries.filter { it.date == dateStr }
                DailyCalorieSummary(
                    date = dateStr,
                    dayLabel = dayName,
                    totalCalories = dayEntries.sumOf { it.calories },
                    totalProteinG = dayEntries.sumOf { it.proteinG },
                    totalCarbsG = dayEntries.sumOf { it.carbsG },
                    totalFatG = dayEntries.sumOf { it.fatG }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Add Food Form & Analysis State
    private val _analysisState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val analysisState: StateFlow<AnalysisUiState> = _analysisState.asStateFlow()

    private val _addFoodInputText = MutableStateFlow("")
    val addFoodInputText: StateFlow<String> = _addFoodInputText.asStateFlow()

    private val _selectedMealType = MutableStateFlow(DateUtils.suggestMealTypeForCurrentTime())
    val selectedMealType: StateFlow<String> = _selectedMealType.asStateFlow()

    // AI Suggestions
    private val _mealSuggestions = MutableStateFlow<List<MealSuggestion>>(emptyList())
    val mealSuggestions: StateFlow<List<MealSuggestion>> = _mealSuggestions.asStateFlow()

    private val _isLoadingSuggestions = MutableStateFlow(false)
    val isLoadingSuggestions: StateFlow<Boolean> = _isLoadingSuggestions.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    fun switchTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun setAddFoodInput(text: String) {
        _addFoodInputText.value = text
    }

    fun setMealType(mealType: String) {
        _selectedMealType.value = mealType
    }

    fun prefillAndNavigateToAddFood(promptText: String) {
        _addFoodInputText.value = promptText
        _analysisState.value = AnalysisUiState.Idle
        _currentTab.value = NavigationTab.ADD_FOOD
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun changeDateBy(days: Long) {
        val current = DateUtils.parseDate(_selectedDate.value)
        val newDate = current.plusDays(days)
        _selectedDate.value = DateUtils.formatDateToString(newDate)
    }

    fun resetToToday() {
        _selectedDate.value = DateUtils.getTodayDateString()
    }

    fun analyzeFood(correctionContext: String? = null) {
        val input = _addFoodInputText.value.trim()
        if (input.isEmpty()) {
            _analysisState.value = AnalysisUiState.Error("Please write what you ate before analyzing.")
            return
        }

        viewModelScope.launch {
            _analysisState.value = AnalysisUiState.Loading
            val meal = _selectedMealType.value
            val result = AiFoodService.analyzeFood(input, meal, correctionContext)
            result.fold(
                onSuccess = { data ->
                    _analysisState.value = AnalysisUiState.Success(data)
                },
                onFailure = { err ->
                    _analysisState.value = AnalysisUiState.Error(
                        err.message ?: "Could not estimate calories. Please try rephrasing."
                    )
                }
            )
        }
    }

    fun saveAnalyzedFood(
        foodName: String,
        quantity: String,
        calories: Int,
        proteinG: Int,
        carbsG: Int,
        fatG: Int,
        confidence: String,
        assumptions: String
    ) {
        viewModelScope.launch {
            val entry = FoodEntry(
                date = DateUtils.getTodayDateString(),
                timestamp = System.currentTimeMillis(),
                mealType = _selectedMealType.value,
                foodName = foodName.ifBlank { "Logged Meal" },
                rawDescription = _addFoodInputText.value,
                estimatedQuantity = quantity.ifBlank { "1 serving" },
                calories = calories.coerceAtLeast(0),
                proteinG = proteinG.coerceAtLeast(0),
                carbsG = carbsG.coerceAtLeast(0),
                fatG = fatG.coerceAtLeast(0),
                confidence = confidence,
                assumptions = assumptions
            )

            repository.insertFoodEntry(entry)
            _analysisState.value = AnalysisUiState.Idle
            _addFoodInputText.value = ""
            _snackbarEvent.emit("Added to today's log! 🔥")
            _currentTab.value = NavigationTab.HOME
        }
    }

    fun discardAnalysis() {
        _analysisState.value = AnalysisUiState.Idle
    }

    fun deleteEntry(entry: FoodEntry) {
        viewModelScope.launch {
            repository.deleteFoodEntry(entry.id)
            _snackbarEvent.emit("Meal removed")
        }
    }

    fun updateEntry(entry: FoodEntry) {
        viewModelScope.launch {
            repository.updateFoodEntry(entry)
            _snackbarEvent.emit("Meal updated")
        }
    }

    fun repeatEntryToday(entry: FoodEntry) {
        viewModelScope.launch {
            val repeated = entry.copy(
                id = 0,
                date = DateUtils.getTodayDateString(),
                timestamp = System.currentTimeMillis(),
                mealType = DateUtils.suggestMealTypeForCurrentTime()
            )
            repository.insertFoodEntry(repeated)
            _snackbarEvent.emit("Repeated ${entry.foodName} for today! 🔥")
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            val todayStr = DateUtils.getTodayDateString()
            val currentMl = todayWaterLog.value?.mlConsumed ?: 0
            val newMl = (currentMl + amountMl).coerceAtLeast(0)
            repository.saveWaterLog(todayStr, newMl)
            _snackbarEvent.emit("Water logged: ${newMl}ml 💧")
        }
    }

    fun fetchMealSuggestions(remainingCalories: Int) {
        viewModelScope.launch {
            _isLoadingSuggestions.value = true
            val suggestions = MealSuggestionService.getSuggestions(remainingCalories)
            _mealSuggestions.value = suggestions
            _isLoadingSuggestions.value = false
        }
    }

    fun logWeight(weightKg: Float) {
        viewModelScope.launch {
            val todayStr = DateUtils.getTodayDateString()
            repository.insertWeightLog(todayStr, weightKg)
            val current = userProfile.value
            if (current != null) {
                repository.saveUserProfile(current.copy(weightKg = weightKg))
            }
            _snackbarEvent.emit("Weight logged: ${weightKg}kg ⚖️")
        }
    }

    fun updateGoalWeight(goalWeightKg: Float) {
        viewModelScope.launch {
            val current = userProfile.value ?: return@launch
            repository.saveUserProfile(current.copy(goalWeightKg = goalWeightKg))
            _snackbarEvent.emit("Goal weight updated: ${goalWeightKg}kg")
        }
    }

    fun updateWaterGoal(waterGoalMl: Int) {
        viewModelScope.launch {
            val current = userProfile.value ?: return@launch
            repository.saveUserProfile(current.copy(waterGoalMl = waterGoalMl))
            _snackbarEvent.emit("Water goal updated: ${waterGoalMl}ml")
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val current = userProfile.value ?: return@launch
            val nextTheme = if (current.themeMode == "dark") "light" else "dark"
            repository.saveUserProfile(current.copy(themeMode = nextTheme))
        }
    }

    fun completeOnboarding(
        name: String,
        age: Int,
        heightCm: Float,
        isMetricHeight: Boolean,
        weightKg: Float,
        isMetricWeight: Boolean,
        sex: String,
        activityLevel: String,
        goal: String,
        goalRate: String
    ) {
        viewModelScope.launch {
            val calc = BmrCalculator.calculate(
                age = age,
                heightCm = heightCm,
                weightKg = weightKg,
                sex = sex,
                activityLevel = activityLevel,
                goal = goal,
                goalRate = goalRate
            )

            val profile = UserProfile(
                id = 1,
                name = name.trim().ifBlank { "Fitness Hero" },
                age = age,
                heightCm = heightCm,
                isMetricHeight = isMetricHeight,
                weightKg = weightKg,
                isMetricWeight = isMetricWeight,
                sex = sex,
                activityLevel = activityLevel,
                goal = goal,
                goalRate = goalRate,
                bmr = calc.bmr,
                tdee = calc.tdee,
                calorieTarget = calc.calorieTarget,
                proteinTargetG = calc.proteinG,
                carbsTargetG = calc.carbsG,
                fatTargetG = calc.fatG,
                waterGoalMl = 2500,
                isOnboarded = true
            )

            repository.saveUserProfile(profile)
            // Log initial weight
            repository.insertWeightLog(DateUtils.getTodayDateString(), weightKg)
            _snackbarEvent.emit("Welcome to MyFit, ${profile.name}! 🎉")
        }
    }

    fun resetProfileForOnboarding() {
        viewModelScope.launch {
            repository.clearUserProfile()
            _snackbarEvent.emit("Reset profile")
        }
    }

    fun updateDailyTargets(calorieTarget: Int, proteinG: Int, carbsG: Int, fatG: Int) {
        viewModelScope.launch {
            val current = userProfile.value ?: return@launch
            val updated = current.copy(
                calorieTarget = calorieTarget,
                proteinTargetG = proteinG,
                carbsTargetG = carbsG,
                fatTargetG = fatG
            )
            repository.saveUserProfile(updated)
            _snackbarEvent.emit("Targets updated")
        }
    }
}
