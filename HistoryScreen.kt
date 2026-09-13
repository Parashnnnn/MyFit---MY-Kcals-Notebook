package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodEntry
import com.example.data.model.UserProfile
import com.example.ui.DailyCalorieSummary
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
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun HistoryScreen(
    profile: UserProfile,
    allEntries: List<FoodEntry>,
    weeklySummaries: List<DailyCalorieSummary>,
    onDeleteEntry: (FoodEntry) -> Unit,
    onRepeatEntry: (FoodEntry) -> Unit,
    onUpdateEntry: (FoodEntry) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Today", "This Week", "This Month"
    var editingMeal by remember { mutableStateOf<FoodEntry?>(null) }

    // Group expanded states
    val expandedDays = remember { mutableStateMapOf<String, Boolean>() }

    // Filter logic
    val today = LocalDate.now()
    val filteredEntries = allEntries.filter { entry ->
        val matchesQuery = if (searchQuery.isBlank()) true else {
            entry.foodName.contains(searchQuery, ignoreCase = true) ||
                    entry.rawDescription.contains(searchQuery, ignoreCase = true) ||
                    entry.mealType.contains(searchQuery, ignoreCase = true)
        }

        val entryDate = DateUtils.parseDate(entry.date)
        val matchesFilter = when (selectedFilter) {
            "Today" -> entry.date == DateUtils.getTodayDateString()
            "This Week" -> ChronoUnit.DAYS.between(entryDate, today) in 0..6
            "This Month" -> ChronoUnit.DAYS.between(entryDate, today) in 0..29
            else -> true
        }

        matchesQuery && matchesFilter
    }

    // Group by date
    val groupedByDate = filteredEntries.groupBy { it.date }

    // Weekly stats
    val weeklyAverage = if (weeklySummaries.isNotEmpty()) {
        val nonZeroDays = weeklySummaries.filter { it.totalCalories > 0 }
        if (nonZeroDays.isNotEmpty()) nonZeroDays.map { it.totalCalories }.average().roundToInt() else 0
    } else 0

    val highestCalDay = weeklySummaries.maxByOrNull { it.totalCalories }
    val highestProteinDay = weeklySummaries.maxByOrNull { it.totalProteinG }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Meal History & Insights",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            )
            Text(
                text = "Review past logs, spot weekly trends, and repeat favourite meals.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
        }

        // Weekly Calorie Bar Chart (Section 11)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_chart_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Last 7 Days Calorie Intake",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Goal: ${profile.calorieTarget} kcal",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = BrandBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Jetpack Compose Canvas Bar Chart
                    WeeklyBarChart(
                        summaries = weeklySummaries,
                        targetCalories = profile.calorieTarget
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats summary pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Weekly Avg", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                            Text(
                                if (weeklyAverage > 0) "~$weeklyAverage kcal/day" else "--",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                        }
                        if (highestProteinDay != null && highestProteinDay.totalProteinG > 0) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Best Protein Day", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                                Text(
                                    "${highestProteinDay.dayLabel} (${highestProteinDay.totalProteinG}g P)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MacroProtein)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search Bar (Section 10)
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search meals (e.g. roti, biryani, oats)...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }

        // Filter Chips: All, Today, This Week, This Month
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Today", "This Week", "This Month").forEach { filter ->
                    val selected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) BrandBlue else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) Color.White else TextSecondary
                            )
                        )
                    }
                }
            }
        }

        // Grouped Entries
        if (groupedByDate.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔍", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No meals match \"$searchQuery\"" else "No meals recorded in this period",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            groupedByDate.forEach { (dateStr, entriesForDay) ->
                val dayCalories = entriesForDay.sumOf { it.calories }
                val dayProtein = entriesForDay.sumOf { it.proteinG }
                val isExpanded = expandedDays[dateStr] ?: true

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_day_card_$dateStr"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header: Date, Total Cal vs Goal, Expand Arrow
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedDays[dateStr] = !isExpanded },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = DateUtils.formatDisplayDate(dateStr),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = TextPrimary
                                        )
                                    )
                                    Text(
                                        text = "${entriesForDay.size} meals • ${dayProtein}g protein",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$dayCalories / ${profile.calorieTarget} kcal",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (dayCalories > profile.calorieTarget) BrandCoral else BrandGreen
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Expandable meal items
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier.padding(top = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    entriesForDay.forEach { entry ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                .clickable { editingMeal = entry }
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = mealTypeEmoji(entry.mealType), fontSize = 18.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = entry.foodName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = TextPrimary
                                                    )
                                                )
                                                Text(
                                                    text = "${entry.mealType} • ${entry.estimatedQuantity} • ${entry.calories} kcal",
                                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                                )
                                            }

                                            // Repeat meal button (Section 10)
                                            IconButton(
                                                onClick = { onRepeatEntry(entry) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Repeat meal today",
                                                    tint = BrandBlue,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            // Delete button
                                            IconButton(
                                                onClick = { onDeleteEntry(entry) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(18.dp)
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
fun WeeklyBarChart(
    summaries: List<DailyCalorieSummary>,
    targetCalories: Int,
    modifier: Modifier = Modifier
) {
    val maxCal = max(targetCalories * 1.25f, (summaries.maxOfOrNull { it.totalCalories } ?: 2000).toFloat())

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            summaries.forEach { summary ->
                val barRatio = if (maxCal > 0) (summary.totalCalories / maxCal).coerceIn(0.04f, 1f) else 0.04f
                val isOver = summary.totalCalories > targetCalories

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (summary.totalCalories > 0) {
                        Text(
                            text = "${summary.totalCalories}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOver) BrandCoral else TextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    // Bar
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height((100 * barRatio).dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                if (summary.totalCalories == 0) {
                                    MaterialTheme.colorScheme.surfaceVariant
                                } else if (isOver) {
                                    BrandCoral
                                } else {
                                    BrandBlue
                                }
                            )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = summary.dayLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    )
                }
            }
        }
    }
}
