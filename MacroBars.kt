package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MacroCarbs
import com.example.ui.theme.MacroCarbsBg
import com.example.ui.theme.MacroFat
import com.example.ui.theme.MacroFatBg
import com.example.ui.theme.MacroProtein
import com.example.ui.theme.MacroProteinBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MacroBarsRow(
    consumedProtein: Int,
    goalProtein: Int,
    consumedCarbs: Int,
    goalCarbs: Int,
    consumedFat: Int,
    goalFat: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MacroCard(
            title = "Protein",
            emoji = "🥩",
            consumed = consumedProtein,
            goal = goalProtein,
            barColor = MacroProtein,
            bgColor = MacroProteinBg,
            modifier = Modifier.weight(1f)
        )
        MacroCard(
            title = "Carbs",
            emoji = "🍚",
            consumed = consumedCarbs,
            goal = goalCarbs,
            barColor = MacroCarbs,
            bgColor = MacroCarbsBg,
            modifier = Modifier.weight(1f)
        )
        MacroCard(
            title = "Fat",
            emoji = "🥑",
            consumed = consumedFat,
            goal = goalFat,
            barColor = MacroFat,
            bgColor = MacroFatBg,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MacroCard(
    title: String,
    emoji: String,
    consumed: Int,
    goal: Int,
    barColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (goal > 0) (consumed.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "macro_progress"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "${consumed}g",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = " / ${goal}g",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress Pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(bgColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(CircleShape)
                    .background(barColor)
            )
        }
    }
}
