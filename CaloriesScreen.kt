package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodEntry
import com.example.data.model.UserProfile
import com.example.ui.components.CalorieCircularProgressRing
import com.example.ui.components.MacroBarsRow
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandCoral
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.MacroCarbs
import com.example.ui.theme.MacroFat
import com.example.ui.theme.MacroProtein
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.DateUtils
import kotlin.math.abs

@Composable
fun CaloriesScreen(
    profile: UserProfile,
    selectedDate: String,
    entries: List<FoodEntry>,
    onChangeDate: (Long) -> Unit,
    onResetToday: () -> Unit,
    onNavigateToAddFood: () -> Unit,
    onDeleteEntry: (FoodEntry) -> Unit,
    onRepeatEntry: (FoodEntry) -> Unit,
    onUpdateEntry: (FoodEntry) -> Unit = {}
) {
    val totalCalories = entries.sumOf { it.calories }
    val totalProtein = entries.sumOf { it.proteinG }
    val totalCarbs = entries.sumOf { it.carbsG }
    val totalFat = entries.sumOf { it.fatG }
    val remaining = profile.calorieTarget - totalCalories
    val isOver = remaining < 0

    var editingMeal by remember { mutableStateOf<FoodEntry?>(null) }

    // Expanded state for meal categories (default all expanded)
    val expandedMeals = remember {
        mutableStateMapOf(
            "Breakfast" to true,
            "Lunch" to true,
            "Snack" to true,
            "Dinner" to true
        )
    }

    val isToday = selectedDate == DateUtils.getTodayDateString()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Date Navigation Header: < [Date] > + "Today" button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onChangeDate(-1) },
                    modifier = Modifier.testTag("calories_prev_date_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Day",
                        tint = TextPrimary
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onResetToday() }
                ) {
                    Text(
                        text = DateUtils.formatDisplayDate(selectedDate),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    )
                    if (!isToday) {
                        Text(
                            text = "Tap to jump to Today",
                            style = MaterialTheme.typography.labelSmall.copy(color = BrandBlue)
                        )
                    }
                }

                IconButton(
                    onClick = { onChangeDate(1) },
                    modifier = Modifier.testTag("calories_next_date_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Day",
                        tint = TextPrimary
                    )
                }
            }
        }

        // Calorie Ring & Target Breakdown Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CalorieCircularProgressRing(
                        consumed = totalCalories,
                        goal = profile.calorieTarget
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("Consumed", "$totalCalories kcal", BrandCoral)
                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
                        StatItem("Target", "${profile.calorieTarget} kcal", BrandBlue)
                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
                        StatItem(
                            if (isOver) "Over" else "Remaining",
                            "${abs(remaining)} kcal",
                            if (isOver) BrandCoral else BrandGreen
                        )
                    }
                }
            }
        }

        // Macro Bars Row
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

        // Meal-by-Meal Breakdown (Section 9)
        val mealCategories = listOf("Breakfast", "Lunch", "Snack", "Dinner")

        mealCategories.forEach { category ->
            val categoryEntries = entries.filter { it.mealType.equals(category, ignoreCase = true) }
            val categoryCalories = categoryEntries.sumOf { it.calories }
            val isExpanded = expandedMeals[category] ?: true

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("meal_section_$category"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header row: Category Title + Subtotal + Expand/Collapse
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedMeals[category] = !isExpanded
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = mealTypeEmoji(category), fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${categoryEntries.size})",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$categoryCalories kcal",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (categoryCalories > 0) BrandBlue else TextSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Collapsible List of items
                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier.padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (categoryEntries.isEmpty()) {
                                    Text(
                                        text = "No items logged for $category",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextMuted,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        ),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                } else {
                                    categoryEntries.forEach { entry ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                .clickable { editingMeal = entry }
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = entry.foodName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = TextPrimary
                                                    )
                                                )
                                                Text(
                                                    text = "${entry.estimatedQuantity} • P: ${entry.proteinG}g C: ${entry.carbsG}g F: ${entry.fatG}g",
                                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp)
                                                )
                                            }

                                            Text(
                                                text = "${entry.calories} kcal",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = BrandCoral
                                                )
                                            )

                                            Spacer(modifier = Modifier.width(4.dp))

                                            IconButton(
                                                onClick = { onRepeatEntry(entry) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Repeat",
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { onDeleteEntry(entry) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Empty state & quick add CTA
        if (entries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🥗", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No meals logged for this date",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Ready to log what you ate?",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToAddFood,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Log Meal Now")
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(90.dp))
        }
    }

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
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}
