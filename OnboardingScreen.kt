package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandCoral
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.BmrCalculator
import kotlin.math.roundToInt

@Composable
fun OnboardingScreen(
    onComplete: (
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
    ) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    val totalSteps = 9

    // State values
    var name by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("25") }
    var isMetricHeight by remember { mutableStateOf(true) }
    var heightCmText by remember { mutableStateOf("172") }
    var heightFeetText by remember { mutableStateOf("5") }
    var heightInchesText by remember { mutableStateOf("8") }

    var isMetricWeight by remember { mutableStateOf(true) }
    var weightKgText by remember { mutableStateOf("70") }
    var weightLbText by remember { mutableStateOf("154") }

    var sex by remember { mutableStateOf("male") }
    var activityLevel by remember { mutableStateOf("moderate") }
    var goal by remember { mutableStateOf("lose") }
    var goalRate by remember { mutableStateOf("moderate") }

    // Helpers to get numeric values
    val age = ageText.toIntOrNull() ?: 25
    val heightCm = if (isMetricHeight) {
        heightCmText.toFloatOrNull() ?: 172f
    } else {
        val ft = heightFeetText.toIntOrNull() ?: 5
        val inc = heightInchesText.toIntOrNull() ?: 8
        BmrCalculator.ftInToCm(ft, inc)
    }

    val weightKg = if (isMetricWeight) {
        weightKgText.toFloatOrNull() ?: 70f
    } else {
        val lbs = weightLbText.toFloatOrNull() ?: 154f
        BmrCalculator.lbsToKg(lbs)
    }

    val calcResult = remember(age, heightCm, weightKg, sex, activityLevel, goal, goalRate) {
        BmrCalculator.calculate(
            age = age,
            heightCm = heightCm,
            weightKg = weightKg,
            sex = sex,
            activityLevel = activityLevel,
            goal = goal,
            goalRate = goalRate
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Progress Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 1) {
                IconButton(
                    onClick = { step-- },
                    modifier = Modifier.testTag("onboarding_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Step $step of $totalSteps",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { step.toFloat() / totalSteps.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(6.dp)
                        .clip(CircleShape),
                    color = BrandBlue,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(48.dp))
        }

        // Animated Step Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut())
                    }
                },
                label = "onboarding_step_transition"
            ) { currentStep ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (currentStep) {
                        1 -> NameStep(name = name, onNameChange = { name = it })
                        2 -> AgeStep(age = ageText, onAgeChange = { ageText = it })
                        3 -> HeightStep(
                            isMetric = isMetricHeight,
                            onToggleMetric = { isMetricHeight = it },
                            cm = heightCmText,
                            onCmChange = { heightCmText = it },
                            feet = heightFeetText,
                            onFeetChange = { heightFeetText = it },
                            inches = heightInchesText,
                            onInchesChange = { heightInchesText = it }
                        )
                        4 -> WeightStep(
                            isMetric = isMetricWeight,
                            onToggleMetric = { isMetricWeight = it },
                            kg = weightKgText,
                            onKgChange = { weightKgText = it },
                            lb = weightLbText,
                            onLbChange = { weightLbText = it }
                        )
                        5 -> SexStep(selectedSex = sex, onSelectSex = { sex = it })
                        6 -> ActivityStep(
                            selectedActivity = activityLevel,
                            onSelectActivity = { activityLevel = it }
                        )
                        7 -> GoalStep(selectedGoal = goal, onSelectGoal = { goal = it })
                        8 -> GoalRateStep(
                            goal = goal,
                            selectedRate = goalRate,
                            onSelectRate = { goalRate = it }
                        )
                        9 -> TargetCalculationStep(
                            name = name,
                            calc = calcResult,
                            sex = sex,
                            goal = goal
                        )
                    }
                }
            }
        }

        // Bottom Action Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = {
                    if (step < totalSteps) {
                        step++
                    } else {
                        onComplete(
                            name,
                            age,
                            heightCm,
                            isMetricHeight,
                            weightKg,
                            isMetricWeight,
                            sex,
                            activityLevel,
                            goal,
                            goalRate
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("onboarding_continue_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text(
                    text = if (step == totalSteps) "Let's Get Started 🚀" else "Continue",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                if (step < totalSteps) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Individual Step Composables
// -------------------------------------------------------------------------

@Composable
private fun NameStep(name: String, onNameChange: (String) -> Unit) {
    Text(
        text = "Welcome to MyFit! 👋",
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        ),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "What should we call you?",
        style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(32.dp))

    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Your Name") },
        placeholder = { Text("e.g. Alex, Rahul, Priya") },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_name_input"),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrandBlue,
            focusedLabelColor = BrandBlue
        )
    )
}

@Composable
private fun AgeStep(age: String, onAgeChange: (String) -> Unit) {
    Text(
        text = "How old are you?",
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "This helps calculate your precise metabolic rate (BMR).",
        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(32.dp))

    OutlinedTextField(
        value = age,
        onValueChange = { if (it.length <= 3 && it.all { char -> char.isDigit() }) onAgeChange(it) },
        label = { Text("Age (years)") },
        placeholder = { Text("25") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_age_input"),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrandBlue,
            focusedLabelColor = BrandBlue
        )
    )
}

@Composable
private fun HeightStep(
    isMetric: Boolean,
    onToggleMetric: (Boolean) -> Unit,
    cm: String,
    onCmChange: (String) -> Unit,
    feet: String,
    onFeetChange: (String) -> Unit,
    inches: String,
    onInchesChange: (String) -> Unit
) {
    Text(
        text = "What is your height?",
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
    )
    Spacer(modifier = Modifier.height(16.dp))

    // Unit toggle
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        UnitToggleButton("cm", isMetric) { onToggleMetric(true) }
        UnitToggleButton("ft / in", !isMetric) { onToggleMetric(false) }
    }

    Spacer(modifier = Modifier.height(28.dp))

    if (isMetric) {
        OutlinedTextField(
            value = cm,
            onValueChange = { if (it.length <= 4) onCmChange(it) },
            label = { Text("Height in cm") },
            placeholder = { Text("172") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_height_cm_input")
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = feet,
                onValueChange = { onFeetChange(it) },
                label = { Text("Feet") },
                placeholder = { Text("5") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = inches,
                onValueChange = { onInchesChange(it) },
                label = { Text("Inches") },
                placeholder = { Text("8") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WeightStep(
    isMetric: Boolean,
    onToggleMetric: (Boolean) -> Unit,
    kg: String,
    onKgChange: (String) -> Unit,
    lb: String,
    onLbChange: (String) -> Unit
) {
    Text(
        text = "What is your weight?",
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
    )
    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        UnitToggleButton("kg", isMetric) { onToggleMetric(true) }
        UnitToggleButton("lb", !isMetric) { onToggleMetric(false) }
    }

    Spacer(modifier = Modifier.height(28.dp))

    if (isMetric) {
        OutlinedTextField(
            value = kg,
            onValueChange = { onKgChange(it) },
            label = { Text("Current weight in kg") },
            placeholder = { Text("70") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_weight_kg_input")
        )
    } else {
        OutlinedTextField(
            value = lb,
            onValueChange = { onLbChange(it) },
            label = { Text("Current weight in lbs") },
            placeholder = { Text("154") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_weight_lb_input")
        )
    }
}

@Composable
private fun SexStep(selectedSex: String, onSelectSex: (String) -> Unit) {
    Text(
        text = "Biological Sex",
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Used exclusively for metabolic BMR formula accuracy.",
        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(32.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SelectionCard(
            title = "Male",
            icon = "👨",
            selected = selectedSex == "male",
            onClick = { onSelectSex("male") },
            modifier = Modifier.weight(1f)
        )
        SelectionCard(
            title = "Female",
            icon = "👩",
            selected = selectedSex == "female",
            onClick = { onSelectSex("female") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActivityStep(selectedActivity: String, onSelectActivity: (String) -> Unit) {
    Text(
        text = "Activity Level",
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "How active are you on a typical week?",
        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
    )
    Spacer(modifier = Modifier.height(20.dp))

    val activities = listOf(
        Triple("sedentary", "Sedentary", "Desk job, little to no exercise (x1.2)"),
        Triple("light", "Lightly Active", "Light exercise 1–3 days/week (x1.375)"),
        Triple("moderate", "Moderately Active", "Moderate exercise 3–5 days/week (x1.55)"),
        Triple("active", "Active", "Hard exercise 6–7 days/week (x1.725)"),
        Triple("very_active", "Very Active", "Hard daily exercise & physical labor (x1.9)")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        activities.forEach { (key, title, desc) ->
            SelectionRowCard(
                title = title,
                subtitle = desc,
                selected = selectedActivity == key,
                onClick = { onSelectActivity(key) }
            )
        }
    }
}

@Composable
private fun GoalStep(selectedGoal: String, onSelectGoal: (String) -> Unit) {
    Text(
        text = "What is your primary goal?",
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "We will tailor your daily calories to match your ambitions.",
        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(24.dp))

    val goals = listOf(
        Triple("lose", "Lose Weight", "Burn fat sustainably with a calorie deficit 🔥"),
        Triple("maintain", "Maintain Weight", "Keep your current weight and build steady habits ⚖️"),
        Triple("gain", "Gain Weight", "Build muscle and increase strength with a surplus 💪")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        goals.forEach { (key, title, desc) ->
            SelectionRowCard(
                title = title,
                subtitle = desc,
                selected = selectedGoal == key,
                onClick = { onSelectGoal(key) }
            )
        }
    }
}

@Composable
private fun GoalRateStep(goal: String, selectedRate: String, onSelectRate: (String) -> Unit) {
    Text(
        text = "Pace of Progress",
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Choose a sustainable rate that fits your lifestyle.",
        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
    )
    Spacer(modifier = Modifier.height(24.dp))

    val rates = if (goal == "maintain") {
        listOf(Triple("moderate", "Balanced Maintenance", "Zero deficit/surplus to maintain steady weight"))
    } else {
        listOf(
            Triple("mild", "Mild Pace", "±250 kcal/day (~0.25 kg / 0.5 lb per week) - Easiest to maintain"),
            Triple("moderate", "Recommended Pace", "±500 kcal/day (~0.5 kg / 1.0 lb per week) - Optimal balance"),
            Triple("aggressive", "Aggressive Pace", "±750 kcal/day (~0.75 kg / 1.5 lb per week) - Faster results")
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        rates.forEach { (key, title, desc) ->
            SelectionRowCard(
                title = title,
                subtitle = desc,
                selected = selectedRate == key,
                onClick = { onSelectRate(key) }
            )
        }
    }
}

@Composable
private fun TargetCalculationStep(
    name: String,
    calc: com.example.util.CalculatedNutritionTarget,
    sex: String,
    goal: String
) {
    Text(
        text = "Your Personalized Plan",
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Calculated using Mifflin-St Jeor equation",
        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
    )
    Spacer(modifier = Modifier.height(20.dp))

    // Main Target Highlight Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DAILY TARGET",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${calc.calorieTarget}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = " kcal / day",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    ),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BMR & TDEE Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Basal BMR", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                    Text("${calc.bmr} kcal", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                }
                Box(modifier = Modifier.width(1.dp).height(28.dp).background(Color.Gray.copy(alpha = 0.3f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total TDEE", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                    Text("${calc.tdee} kcal", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Default Macro Split
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Target Macro Split (30% P / 40% C / 30% F)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroPillItem("🥩 Protein", "${calc.proteinG}g", "30%")
                MacroPillItem("🍚 Carbs", "${calc.carbsG}g", "40%")
                MacroPillItem("🥑 Fat", "${calc.fatG}g", "30%")
            }
        }
    }

    if (calc.isCappedAtMinimum) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(BrandCoral.copy(alpha = 0.12f))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = BrandCoral, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Target adjusted to safe minimum of ${if (sex == "female") "1,200" else "1,500"} kcal/day for your well-being.",
                style = MaterialTheme.typography.labelSmall.copy(color = BrandCoral)
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Friendly medical disclaimer
    Text(
        text = "Note: Calorie estimates are for guidance. Consult a healthcare professional or registered dietitian for specific medical conditions.",
        style = MaterialTheme.typography.labelSmall.copy(
            color = TextSecondary,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
private fun MacroPillItem(label: String, grams: String, percent: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
        Text(grams, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
        Text(percent, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = TextSecondary))
    }
}

@Composable
private fun UnitToggleButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) BrandBlue else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else TextSecondary
            )
        )
    }
}

@Composable
private fun SelectionCard(
    title: String,
    icon: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) BrandBlue else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) BrandBlue else TextPrimary
                )
            )
        }
    }
}

@Composable
private fun SelectionRowCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) BrandBlue else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (selected) BrandBlue else TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = BrandBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
