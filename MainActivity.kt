package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.NavigationTab
import com.example.ui.MyFitViewModel
import com.example.ui.screens.AddFoodScreen
import com.example.ui.screens.CaloriesScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandCoral
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MyFitViewModel = viewModel()
            val userProfile by viewModel.userProfile.collectAsState()
            val isDark = userProfile?.themeMode == "dark"

            MyApplicationTheme(darkTheme = isDark) {
                MyFitApp(viewModel = viewModel, isDark = isDark)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFitApp(viewModel: MyFitViewModel = viewModel(), isDark: Boolean = false) {
    val userProfile by viewModel.userProfile.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val todayEntries by viewModel.todayEntries.collectAsState()
    val selectedDateEntries by viewModel.selectedDateEntries.collectAsState()
    val allHistoryEntries by viewModel.allHistoryEntries.collectAsState()
    val todayWaterLog by viewModel.todayWaterLog.collectAsState()
    val streakCount by viewModel.streakCount.collectAsState()
    val weeklySummaries by viewModel.weeklyDailySummaries.collectAsState()
    val allWeightLogs by viewModel.allWeightLogs.collectAsState()

    val analysisState by viewModel.analysisState.collectAsState()
    val addFoodInput by viewModel.addFoodInputText.collectAsState()
    val selectedMealType by viewModel.selectedMealType.collectAsState()

    val mealSuggestions by viewModel.mealSuggestions.collectAsState()
    val isLoadingSuggestions by viewModel.isLoadingSuggestions.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showSettingsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val isOnboarded = userProfile?.isOnboarded == true

    if (!isOnboarded) {
        OnboardingScreen(
            onComplete = { name, age, heightCm, isMetricHeight, weightKg, isMetricWeight, sex, activityLevel, goal, goalRate ->
                viewModel.completeOnboarding(
                    name = name,
                    age = age,
                    heightCm = heightCm,
                    isMetricHeight = isMetricHeight,
                    weightKg = weightKg,
                    isMetricWeight = isMetricWeight,
                    sex = sex,
                    activityLevel = activityLevel,
                    goal = goal,
                    goalRate = goalRate
                )
            }
        )
    } else {
        val profile = userProfile!!
        val coroutineScope = rememberCoroutineScope()
        val tabs = listOf(NavigationTab.HOME, NavigationTab.ADD_FOOD, NavigationTab.CALORIES, NavigationTab.HISTORY)
        val pagerState = rememberPagerState(initialPage = currentTab.ordinal) { tabs.size }

        LaunchedEffect(currentTab) {
            if (pagerState.currentPage != currentTab.ordinal) {
                pagerState.animateScrollToPage(currentTab.ordinal)
            }
        }

        LaunchedEffect(pagerState.currentPage) {
            if (currentTab.ordinal != pagerState.currentPage) {
                viewModel.switchTab(tabs[pagerState.currentPage])
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_myfit_brand_logo),
                                contentDescription = "MyFit Logo",
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "MyFit",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.toggleTheme() },
                            modifier = Modifier.testTag("top_bar_theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDark) "Switch to light mode" else "Switch to dark mode",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier.testTag("top_bar_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                val activeIndex = pagerState.currentPage
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("main_bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = activeIndex == NavigationTab.HOME.ordinal,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(NavigationTab.HOME.ordinal)
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandBlue,
                            selectedTextColor = BrandBlue,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    NavigationBarItem(
                        selected = activeIndex == NavigationTab.ADD_FOOD.ordinal,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(NavigationTab.ADD_FOOD.ordinal)
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.AddCircle,
                                contentDescription = "Add Food",
                                modifier = Modifier.size(26.dp)
                            )
                        },
                        label = { Text("Add Food", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandBlue,
                            selectedTextColor = BrandBlue,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_item_add_food")
                    )

                    NavigationBarItem(
                        selected = activeIndex == NavigationTab.CALORIES.ordinal,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(NavigationTab.CALORIES.ordinal)
                            }
                        },
                        icon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = "Calories") },
                        label = { Text("Calories", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandCoral,
                            selectedTextColor = BrandCoral,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_item_calories")
                    )

                    NavigationBarItem(
                        selected = activeIndex == NavigationTab.HISTORY.ordinal,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(NavigationTab.HISTORY.ordinal)
                            }
                        },
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandBlue,
                            selectedTextColor = BrandBlue,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_item_history")
                    )
                }
            }
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { page ->
                when (tabs[page]) {
                    NavigationTab.HOME -> {
                        HomeScreen(
                            profile = profile,
                            todayEntries = todayEntries,
                            todayWaterMl = todayWaterLog?.mlConsumed ?: 0,
                            streakCount = streakCount,
                            mealSuggestions = mealSuggestions,
                            isLoadingSuggestions = isLoadingSuggestions,
                            allWeightLogs = allWeightLogs,
                            onNavigateToAddFood = { viewModel.switchTab(NavigationTab.ADD_FOOD) },
                            onPrefillExample = { prompt -> viewModel.prefillAndNavigateToAddFood(prompt) },
                            onAddWater = { ml -> viewModel.addWater(ml) },
                            onLogWeight = { w -> viewModel.logWeight(w) },
                            onUpdateGoalWeight = { g -> viewModel.updateGoalWeight(g) },
                            onRequestSuggestions = { remaining -> viewModel.fetchMealSuggestions(remaining) },
                            onDeleteEntry = { entry -> viewModel.deleteEntry(entry) },
                            onRepeatEntry = { entry -> viewModel.repeatEntryToday(entry) },
                            onUpdateEntry = { entry -> viewModel.updateEntry(entry) }
                        )
                    }

                    NavigationTab.ADD_FOOD -> {
                        AddFoodScreen(
                            inputText = addFoodInput,
                            selectedMealType = selectedMealType,
                            analysisState = analysisState,
                            onInputChange = { text -> viewModel.setAddFoodInput(text) },
                            onMealTypeChange = { meal -> viewModel.setMealType(meal) },
                            onAnalyze = { correction -> viewModel.analyzeFood(correction) },
                            onSaveFood = { foodName, quantity, calories, protein, carbs, fat, confidence, assumptions ->
                                viewModel.saveAnalyzedFood(
                                    foodName = foodName,
                                    quantity = quantity,
                                    calories = calories,
                                    proteinG = protein,
                                    carbsG = carbs,
                                    fatG = fat,
                                    confidence = confidence,
                                    assumptions = assumptions
                                )
                            },
                            onDiscard = { viewModel.discardAnalysis() }
                        )
                    }

                    NavigationTab.CALORIES -> {
                        CaloriesScreen(
                            profile = profile,
                            selectedDate = selectedDate,
                            entries = selectedDateEntries,
                            onChangeDate = { days -> viewModel.changeDateBy(days) },
                            onResetToday = { viewModel.resetToToday() },
                            onNavigateToAddFood = { viewModel.switchTab(NavigationTab.ADD_FOOD) },
                            onDeleteEntry = { entry -> viewModel.deleteEntry(entry) },
                            onRepeatEntry = { entry -> viewModel.repeatEntryToday(entry) },
                            onUpdateEntry = { entry -> viewModel.updateEntry(entry) }
                        )
                    }

                    NavigationTab.HISTORY -> {
                        HistoryScreen(
                            profile = profile,
                            allEntries = allHistoryEntries,
                            weeklySummaries = weeklySummaries,
                            onDeleteEntry = { entry -> viewModel.deleteEntry(entry) },
                            onRepeatEntry = { entry -> viewModel.repeatEntryToday(entry) },
                            onUpdateEntry = { entry -> viewModel.updateEntry(entry) }
                        )
                    }
                }
            }
        }

        if (showSettingsDialog) {
            SettingsDialog(
                profile = profile,
                onDismiss = { showSettingsDialog = false },
                onSaveTargets = { cal, p, c, f ->
                    viewModel.updateDailyTargets(cal, p, c, f)
                },
                onResetOnboarding = {
                    viewModel.resetProfileForOnboarding()
                }
            )
        }
    }
}
