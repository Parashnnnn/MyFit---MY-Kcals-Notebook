package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.MealSuggestion
import com.example.data.model.FoodEntry
import com.example.data.model.UserProfile
import com.example.data.model.WeightLog
import com.example.ui.components.CalorieCircularProgressRing
import com.example.ui.components.MacroBarsRow
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandCoral
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.MacroCarbs
import com.example.ui.theme.MacroFat
import com.example.ui.theme.MacroProtein
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WaterBlue
import com.example.ui.theme.WaterBlueBg
import com.example.util.DateUtils

@Composable
fun HomeScreen(
    profile: UserProfile,
    todayEntries: List<FoodEntry>,
    todayWaterMl: Int,
    streakCount: Int,
    mealSuggestions: List<MealSuggestion>,
    isLoadingSuggestions: Boolean,
    allWeightLogs: List<WeightLog> = emptyList(),
    onNavigateToAddFood: () -> Unit,
    onPrefillExample: (String) -> Unit,
    onAddWater: (Int) -> Unit,
    onLogWeight: (Float) -> Unit = {},
    onUpdateGoalWeight: (Float) -> Unit = {},
    onRequestSuggestions: (Int) -> Unit,
    onDeleteEntry: (FoodEntry) -> Unit,
    onRepeatEntry: (FoodEntry) -> Unit,
    onUpdateEntry: (FoodEntry) -> Unit = {}
) {
    val totalCalories = todayEntries.sumOf { it.calories }
    val totalProtein = todayEntries.sumOf { it.proteinG }
    val totalCarbs = todayEntries.sumOf { it.carbsG }
    val totalFat = todayEntries.sumOf { it.fatG }
    val remainingCalories = (profile.calorieTarget - totalCalories).coerceAtLeast(0)

    var showSuggestionsDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var editingMeal by remember { mutableStateOf<FoodEntry?>(null) }
    var selectedMealForDetail by remember { mutableStateOf<FoodEntry?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Header Row: Greeting + Streak Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = DateUtils.getGreetingForCurrentTime(profile.name),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = DateUtils.formatDisplayDate(DateUtils.getTodayDateString()),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // Subtle Streak Badge (fire emoji removed from right side as per Section 21)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(BrandCoral.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (streakCount > 0) "$streakCount Day Streak" else "Start Streak",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BrandCoral
                        )
                    )
                }
            }
        }

        // Calorie Circular Ring Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_calorie_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Today's Energy Balance",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CalorieCircularProgressRing(
                        consumed = totalCalories,
                        goal = profile.calorieTarget
                    )
                }
            }
        }

        // Three Macro Bars
        item {
            MacroBarsRow(
                consumedProtein = totalProtein,
                goalProtein = profile.proteinTargetG,
                consumedCarbs = totalCarbs,
                goalCarbs = profile.carbsTargetG,
                consumedFat = totalFat,
                goalFat = profile.fatTargetG
            )
        }

        // Big Prominent Add Food Action Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToAddFood)
                    .testTag("home_add_food_banner"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BrandBlue),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Food",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Log What You Ate",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Describe in Hindi, Hinglish or English",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFFFD166),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Tappable Example Prompts (Section 5)
        item {
            Column {
                Text(
                    text = "Quick logging examples",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                val examples = listOf(
                    "2 rotis, dal and chicken curry",
                    "1 plate chicken biryani",
                    "2 eggs and 2 slices of bread",
                    "1 bowl oats with milk and banana"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    examples.take(2).forEach { prompt ->
                        QuickPromptChip(
                            text = prompt,
                            onClick = { onPrefillExample(prompt) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    examples.drop(2).forEach { prompt ->
                        QuickPromptChip(
                            text = prompt,
                            onClick = { onPrefillExample(prompt) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Water Quick Tracker (Section 14)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💧", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Water Hydration",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        Text(
                            text = "${todayWaterMl} / ${profile.waterGoalMl} ml",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = WaterBlue
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val waterProgress = (todayWaterMl.toFloat() / profile.waterGoalMl.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { waterProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = WaterBlue,
                        trackColor = WaterBlueBg
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { onAddWater(250) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+250 ml", fontWeight = FontWeight.SemiBold)
                        }
                        FilledTonalButton(
                            onClick = { onAddWater(500) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+500 ml", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Weight Tracker Card (Section 14)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showWeightDialog = true },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrandGreen.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚖️", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Weight & Trend",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "Current: ${profile.weightKg} kg • Target: ${profile.goalWeightKg} kg",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { showWeightDialog = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Track", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // AI Meal Suggestions trigger (Section 13)
        if (remainingCalories > 150) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onRequestSuggestions(remainingCalories)
                            showSuggestionsDialog = true
                        },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ),
                    border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "💡", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "You have ~$remainingCalories kcal left",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlue
                                )
                            )
                            Text(
                                text = "Tap for healthy, high-protein meal ideas ✨",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }
        }

        // Recent Meals Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Meals",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                if (todayEntries.isNotEmpty()) {
                    Text(
                        text = "${todayEntries.size} logged today",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        if (todayEntries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🍽️", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No meals logged yet today",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tell MyFit what you had for breakfast or lunch to get started!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                    }
                }
            }
        } else {
            items(todayEntries.take(4)) { meal ->
                MealItemCard(
                    meal = meal,
                    onClick = { editingMeal = meal },
                    onDelete = { onDeleteEntry(meal) },
                    onRepeat = { onRepeatEntry(meal) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom bar
        }
    }

    // Weight Tracking Dialog (Section 14)
    if (showWeightDialog) {
        WeightTrackingDialog(
            profile = profile,
            weightLogs = allWeightLogs,
            onDismiss = { showWeightDialog = false },
            onLogWeight = { w -> onLogWeight(w) },
            onUpdateGoalWeight = { g -> onUpdateGoalWeight(g) }
        )
    }

    // Edit Meal Dialog (Section 9)
    editingMeal?.let { meal ->
        EditMealDialog(
            entry = meal,
            onDismiss = { editingMeal = null },
            onSave = { updated ->
                onUpdateEntry(updated)
                editingMeal = null
            },
            onDelete = { toDelete ->
                onDeleteEntry(toDelete)
                editingMeal = null
            }
        )
    }

    // AI Suggestions Dialog (Section 13)
    if (showSuggestionsDialog) {
        AlertDialog(
            onDismissRequest = { showSuggestionsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💡", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "High-Protein Meal Ideas",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Ideas tailored for your ~$remainingCalories kcal budget (ideas only, not medical advice):",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    if (isLoadingSuggestions) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = BrandBlue)
                        }
                    } else if (mealSuggestions.isEmpty()) {
                        Text("No suggestions available at this moment.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        mealSuggestions.forEach { suggestion ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = suggestion.title,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "~${suggestion.estimatedCalories} kcal",
                                            style = MaterialTheme.typography.labelSmall.copy(color = BrandCoral, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = suggestion.description,
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "🥩 ~${suggestion.estimatedProteinG}g protein",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MacroProtein, fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSuggestionsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun QuickPromptChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("quick_prompt_chip"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "👉", fontSize = 12.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MealItemCard(
    meal: FoodEntry,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRepeat: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("meal_item_card_${meal.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Meal emoji avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = mealTypeEmoji(meal.mealType), fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.foodName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${meal.mealType} • ${meal.estimatedQuantity}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "P: ${meal.proteinG}g",
                        style = MaterialTheme.typography.labelSmall.copy(color = MacroProtein, fontSize = 11.sp)
                    )
                    Text(
                        text = "C: ${meal.carbsG}g",
                        style = MaterialTheme.typography.labelSmall.copy(color = MacroCarbs, fontSize = 11.sp)
                    )
                    Text(
                        text = "F: ${meal.fatG}g",
                        style = MaterialTheme.typography.labelSmall.copy(color = MacroFat, fontSize = 11.sp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${meal.calories} kcal",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandCoral
                    )
                )
                IconButton(
                    onClick = onRepeat,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Repeat",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

fun mealTypeEmoji(mealType: String): String {
    return when (mealType.lowercase()) {
        "breakfast" -> "🌅"
        "lunch" -> "☀️"
        "snack" -> "☕"
        "dinner" -> "🌙"
        else -> "🍽️"
    }
}
