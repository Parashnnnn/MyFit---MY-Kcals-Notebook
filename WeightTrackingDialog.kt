package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.WeightLog
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.DateUtils
import kotlin.math.max
import kotlin.math.min

@Composable
fun WeightTrackingDialog(
    profile: UserProfile,
    weightLogs: List<WeightLog>,
    onDismiss: () -> Unit,
    onLogWeight: (Float) -> Unit,
    onUpdateGoalWeight: (Float) -> Unit
) {
    var inputWeight by remember { mutableStateOf(profile.weightKg.toString()) }
    var inputGoal by remember { mutableStateOf(profile.goalWeightKg.toString()) }

    // Sorted chronological logs for line chart
    val sortedLogs = remember(weightLogs) {
        weightLogs.sortedBy { it.timestamp }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚖️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Weight Tracker",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Current & Goal Quick Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Current", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                            Text(
                                text = "${profile.weightKg} kg",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandBlue
                                )
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Target Goal", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                            Text(
                                text = "${profile.goalWeightKg} kg",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandGreen
                                )
                            )
                        }
                    }
                }

                // Trend Line Chart
                if (sortedLogs.size >= 2) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Weight Trend",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            WeightTrendChart(
                                logs = sortedLogs,
                                goalWeight = profile.goalWeightKg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                            )
                        }
                    }
                }

                // Log new weight row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputWeight,
                        onValueChange = { inputWeight = it },
                        label = { Text("Log Weight (kg)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            val w = inputWeight.toFloatOrNull()
                            if (w != null && w > 20f && w < 300f) {
                                onLogWeight(w)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Log")
                    }
                }

                // Editable Goal Weight
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputGoal,
                        onValueChange = { inputGoal = it },
                        label = { Text("Target Goal (kg)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            val g = inputGoal.toFloatOrNull()
                            if (g != null && g > 20f && g < 300f) {
                                onUpdateGoalWeight(g)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Set Goal")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
fun WeightTrendChart(
    logs: List<WeightLog>,
    goalWeight: Float,
    modifier: Modifier = Modifier
) {
    val weights = logs.map { it.weightKg }
    val minW = (min(weights.minOrNull() ?: 50f, goalWeight) - 2f).coerceAtLeast(0f)
    val maxW = max(weights.maxOrNull() ?: 90f, goalWeight) + 2f
    val range = (maxW - minW).coerceAtLeast(1f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stepX = width / (logs.size - 1).coerceAtLeast(1)

        // Draw goal dashed line
        val goalY = height - ((goalWeight - minW) / range) * height
        drawLine(
            color = Color(0xFF10B981).copy(alpha = 0.6f),
            start = Offset(0f, goalY),
            end = Offset(width, goalY),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )

        // Path of points
        val path = Path()
        val points = logs.mapIndexed { index, item ->
            val x = index * stepX
            val y = height - ((item.weightKg - minW) / range) * height
            Offset(x, y)
        }

        points.forEachIndexed { index, pt ->
            if (index == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
        }

        drawPath(
            path = path,
            color = Color(0xFF1A68FF),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw circles at data points
        points.forEach { pt ->
            drawCircle(
                color = Color(0xFF1A68FF),
                radius = 5.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = Color.White,
                radius = 2.5.dp.toPx(),
                center = pt
            )
        }
    }
}
