package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.NutritionAnalysisResult
import com.example.ui.AnalysisUiState
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandCoral
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.MacroCarbs
import com.example.ui.theme.MacroCarbsBg
import com.example.ui.theme.MacroFat
import com.example.ui.theme.MacroFatBg
import com.example.ui.theme.MacroProtein
import com.example.ui.theme.MacroProteinBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun AddFoodScreen(
    inputText: String,
    selectedMealType: String,
    analysisState: AnalysisUiState,
    onInputChange: (String) -> Unit,
    onMealTypeChange: (String) -> Unit,
    onAnalyze: (correctionContext: String?) -> Unit,
    onSaveFood: (
        foodName: String,
        quantity: String,
        calories: Int,
        proteinG: Int,
        carbsG: Int,
        fatG: Int,
        confidence: String,
        assumptions: String
    ) -> Unit,
    onDiscard: () -> Unit
) {
    val placeholders = listOf(
        "What did you eat? e.g. 2 rotis, dal and chicken curry",
        "What did you eat? e.g. 1 plate chicken biryani",
        "What did you eat? e.g. 2 eggs and 2 slices of bread",
        "What did you eat? e.g. masala dosa with coconut chutney",
        "What did you eat? e.g. thoda rice, rajma and salad"
    )

    var placeholderIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3500)
            placeholderIndex = (placeholderIndex + 1) % placeholders.size
        }
    }

    // Editable fields for when analysis result is displayed
    var editCalories by remember { mutableStateOf<Int?>(null) }
    var editProtein by remember { mutableStateOf<Int?>(null) }
    var editCarbs by remember { mutableStateOf<Int?>(null) }
    var editFat by remember { mutableStateOf<Int?>(null) }
    var editFoodName by remember { mutableStateOf<String?>(null) }
    var editQuantity by remember { mutableStateOf<String?>(null) }

    // Dialog state for manually editing a macro
    var showEditDialogFor by remember { mutableStateOf<String?>(null) }
    var editDialogValue by remember { mutableStateOf("") }

    // Correction field (e.g. "actually it was 3 rotis")
    var correctionText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title & Description
        Text(
            text = "Log Meal",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
        )
        Text(
            text = "Tell us what you ate in English, Hindi, or Hinglish. AI will calculate your macros.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
        )

        // Meal Type Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Breakfast", "Lunch", "Snack", "Dinner").forEach { type ->
                val isSelected = selectedMealType.equals(type, ignoreCase = true)
                MealTypePill(
                    label = type,
                    isSelected = isSelected,
                    onClick = { onMealTypeChange(type) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Single Large Text Input
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            placeholder = {
                Text(
                    text = placeholders[placeholderIndex],
                    style = MaterialTheme.typography.bodyLarge.copy(color = TextMuted)
                )
            },
            minLines = 3,
            maxLines = 6,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_food_input_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Analyze Button (Disabled while loading)
        val isLoading = analysisState is AnalysisUiState.Loading
        Button(
            onClick = { onAnalyze(null) },
            enabled = !isLoading && inputText.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("analyze_food_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Analyzing nutrition with AI...",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFFFD166),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Analyze Meal",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
            }
        }

        // Error Banner
        if (analysisState is AnalysisUiState.Error) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandCoral.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, BrandCoral.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = BrandCoral)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Analysis Notice",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = BrandCoral)
                        )
                        Text(
                            text = analysisState.message,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                        )
                    }
                    TextButton(onClick = { onAnalyze(null) }) {
                        Text("Retry", fontWeight = FontWeight.Bold, color = BrandCoral)
                    }
                }
            }
        }

        // Result Card (Section 6)
        if (analysisState is AnalysisUiState.Success) {
            val result = analysisState.result

            val currentFoodName = editFoodName ?: result.foodName
            val currentQuantity = editQuantity ?: result.estimatedQuantity
            val currentCalories = editCalories ?: result.calories
            val currentProtein = editProtein ?: result.proteinG
            val currentCarbs = editCarbs ?: result.carbsG
            val currentFat = editFat ?: result.fatG

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analysis_result_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header with Estimate Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentFoodName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Portion: $currentQuantity",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                            )
                        }

                        // Approximate Estimate Badge (Section 6)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Estimate",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlue
                                )
                            )
                        }
                    }

                    // Tip: tap to override
                    Text(
                        text = "💡 Tap any macro number to manually override it:",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )

                    // 4 Tappable Number Cards (Calories, Protein, Carbs, Fat)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EditableNutrientChip(
                            emoji = "🔥",
                            label = "Calories",
                            value = "$currentCalories kcal",
                            color = BrandCoral,
                            onClick = {
                                showEditDialogFor = "Calories"
                                editDialogValue = currentCalories.toString()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        EditableNutrientChip(
                            emoji = "🥩",
                            label = "Protein",
                            value = "${currentProtein}g",
                            color = MacroProtein,
                            onClick = {
                                showEditDialogFor = "Protein"
                                editDialogValue = currentProtein.toString()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EditableNutrientChip(
                            emoji = "🍚",
                            label = "Carbs",
                            value = "${currentCarbs}g",
                            color = MacroCarbs,
                            onClick = {
                                showEditDialogFor = "Carbs"
                                editDialogValue = currentCarbs.toString()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        EditableNutrientChip(
                            emoji = "🥑",
                            label = "Fat",
                            value = "${currentFat}g",
                            color = MacroFat,
                            onClick = {
                                showEditDialogFor = "Fat"
                                editDialogValue = currentFat.toString()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Assumptions section if present
                    if (result.assumptions.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Assumptions Made:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            result.assumptions.forEach { assumption ->
                                Text(
                                    text = "• $assumption",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }

                    // Correction Input: e.g. "actually it was 3 rotis" (Section 6)
                    Column {
                        Text(
                            text = "Need an AI recalculation?",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = correctionText,
                                onValueChange = { correctionText = it },
                                placeholder = { Text("e.g. actually it was 3 rotis", fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilledTonalButton(
                                onClick = {
                                    if (correctionText.isNotBlank()) {
                                        onAnalyze(correctionText)
                                        correctionText = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Recalculate", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Final Action Buttons: Add to My Calories (Primary) & Discard
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDiscard,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Text("Discard", color = TextSecondary)
                        }

                        Button(
                            onClick = {
                                onSaveFood(
                                    currentFoodName,
                                    currentQuantity,
                                    currentCalories,
                                    currentProtein,
                                    currentCarbs,
                                    currentFat,
                                    result.confidence,
                                    result.assumptions.joinToString("; ")
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1.6f)
                                .height(50.dp)
                                .testTag("add_to_my_calories_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                        ) {
                            Text(
                                text = "Add to My Calories",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(90.dp))
    }

    // Number override dialog
    showEditDialogFor?.let { nutrientKey ->
        AlertDialog(
            onDismissRequest = { showEditDialogFor = null },
            title = { Text("Edit $nutrientKey") },
            text = {
                OutlinedTextField(
                    value = editDialogValue,
                    onValueChange = { editDialogValue = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    label = { Text("Enter $nutrientKey") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val num = editDialogValue.toIntOrNull() ?: 0
                    when (nutrientKey) {
                        "Calories" -> editCalories = num.coerceAtLeast(0)
                        "Protein" -> editProtein = num.coerceAtLeast(0)
                        "Carbs" -> editCarbs = num.coerceAtLeast(0)
                        "Fat" -> editFat = num.coerceAtLeast(0)
                    }
                    showEditDialogFor = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialogFor = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EditableNutrientChip(
    emoji: String,
    label: String,
    value: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "$emoji $label", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = TextMuted,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            )
        }
    }
}

@Composable
fun MealTypePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) BrandBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextSecondary
            )
        )
    }
}
