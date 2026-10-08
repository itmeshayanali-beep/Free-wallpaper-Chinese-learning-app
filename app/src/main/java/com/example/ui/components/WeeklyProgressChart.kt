package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DailyMastery(
    val dayLabel: String,       // "Mon", "Tue", etc.
    val fullDate: String,       // "Monday, Oct 23"
    val wordsMastered: Int,     // Count of new words learned
    val japaneseWords: Int,     // Japanese breakdown
    val chineseWords: Int,      // Chinese breakdown
    val isToday: Boolean = false
)

@Composable
fun WeeklyProgressChart(
    modifier: Modifier = Modifier,
    dailyData: List<DailyMastery> = remember { defaultWeeklyData() },
    dailyTargetGoal: Int = 8
) {
    var selectedDayIndex by remember { mutableIntStateOf(dailyData.indexOfFirst { it.isToday }.coerceAtLeast(3)) }
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    val totalWordsWeek = remember(dailyData) { dailyData.sumOf { it.wordsMastered } }
    val averageDaily = remember(dailyData) { (totalWordsWeek / 7.0f * 10).toInt() / 10f }
    val selectedDay = dailyData.getOrNull(selectedDayIndex) ?: dailyData.last()

    Column(modifier = modifier.fillMaxWidth()) {
        // Weekly Summary Metrics Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryMetricCard(
                modifier = Modifier.weight(1f),
                title = "Total Mastered",
                value = "$totalWordsWeek words",
                subtext = "This week",
                icon = Icons.Default.TrendingUp,
                accentColor = MaterialTheme.colorScheme.primary
            )
            SummaryMetricCard(
                modifier = Modifier.weight(1f),
                title = "Daily Average",
                value = "$averageDaily / day",
                subtext = "Goal: $dailyTargetGoal words",
                icon = Icons.Default.EmojiEvents,
                accentColor = MaterialTheme.colorScheme.secondary
            )
        }

        // Main Chart Card
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .testTag("weekly_progress_chart_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header with Selected Day Inspector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Word Mastery",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap any bar to inspect daily breakdown",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${selectedDay.dayLabel}: ${selectedDay.wordsMastered} words",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Canvas Bar Chart
                val textMeasurer = rememberTextMeasurer()
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant
                val surfaceContainer = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(dailyData) {
                                detectTapGestures { offset ->
                                    val barSlotWidth = size.width / dailyData.size
                                    val clickedIndex = (offset.x / barSlotWidth).toInt().coerceIn(0, dailyData.size - 1)
                                    selectedDayIndex = clickedIndex
                                }
                            }
                    ) {
                        val chartHeight = size.height - 35.dp.toPx()
                        val chartWidth = size.width
                        val maxWords = (dailyData.maxOf { it.wordsMastered }.coerceAtLeast(dailyTargetGoal) + 4)
                        val slotWidth = chartWidth / dailyData.size
                        val barWidth = slotWidth * 0.48f

                        // Draw Target Goal Benchmark Line
                        val goalY = chartHeight - (dailyTargetGoal.toFloat() / maxWords.toFloat()) * chartHeight
                        drawLine(
                            color = primaryColor.copy(alpha = 0.45f),
                            start = Offset(0f, goalY),
                            end = Offset(chartWidth, goalY),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )

                        // Draw Goal Label
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "GOAL $dailyTargetGoal",
                            topLeft = Offset(8.dp.toPx(), goalY - 14.dp.toPx()),
                            style = TextStyle(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor.copy(alpha = 0.8f)
                            )
                        )

                        // Draw Bars for each day
                        dailyData.forEachIndexed { index, item ->
                            val isSelected = index == selectedDayIndex
                            val barHeight = (item.wordsMastered.toFloat() / maxWords.toFloat()) * chartHeight * animProgress.value
                            val barLeft = index * slotWidth + (slotWidth - barWidth) / 2f
                            val barTop = chartHeight - barHeight

                            // Background pill track
                            drawRoundRect(
                                color = surfaceContainer,
                                topLeft = Offset(barLeft, 0f),
                                size = Size(barWidth, chartHeight),
                                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                            )

                            // Active gradient bar
                            val barGradient = Brush.verticalGradient(
                                colors = if (isSelected) {
                                    listOf(primaryColor, primaryColor.copy(alpha = 0.7f))
                                } else {
                                    listOf(secondaryColor.copy(alpha = 0.9f), secondaryColor.copy(alpha = 0.55f))
                                },
                                startY = barTop,
                                endY = chartHeight
                            )

                            drawRoundRect(
                                brush = barGradient,
                                topLeft = Offset(barLeft, barTop),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                            )

                            // Value on top of bar
                            if (barHeight > 18.dp.toPx()) {
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = "${item.wordsMastered}",
                                    topLeft = Offset(
                                        barLeft + barWidth / 2f - 6.dp.toPx(),
                                        barTop + 4.dp.toPx()
                                    ),
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }

                            // Day of week label below chart
                            val textLayout = textMeasurer.measure(
                                text = item.dayLabel,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) primaryColor else onSurfaceColor
                                )
                            )
                            drawText(
                                textLayoutResult = textLayout,
                                topLeft = Offset(
                                    index * slotWidth + (slotWidth - textLayout.size.width) / 2f,
                                    chartHeight + 8.dp.toPx()
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Detailed Selected Day Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedDay.fullDate,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "🇯🇵 ${selectedDay.japaneseWords} Japanese words · 🇨🇳 ${selectedDay.chineseWords} Chinese words",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val metGoal = selectedDay.wordsMastered >= dailyTargetGoal
                        Surface(
                            shape = CircleShape,
                            color = if (metGoal) Color(0xFF2E7D32).copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (metGoal) "✓ Goal Met" else "In Progress",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (metGoal) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

private fun defaultWeeklyData(): List<DailyMastery> = listOf(
    DailyMastery("Mon", "Monday, Oct 23", 6, 4, 2),
    DailyMastery("Tue", "Tuesday, Oct 24", 9, 5, 4),
    DailyMastery("Wed", "Wednesday, Oct 25", 5, 3, 2),
    DailyMastery("Thu", "Thursday, Oct 26", 12, 7, 5),
    DailyMastery("Fri", "Friday, Oct 27", 8, 4, 4),
    DailyMastery("Sat", "Saturday, Oct 28", 15, 8, 7),
    DailyMastery("Sun", "Sunday, Oct 29", 10, 6, 4, isToday = true)
)
