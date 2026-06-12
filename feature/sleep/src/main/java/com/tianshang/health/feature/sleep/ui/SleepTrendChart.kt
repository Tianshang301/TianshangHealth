package com.tianshang.health.feature.sleep.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.database.entity.DailyHealth
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SleepTrendChart(
    data: List<DailyHealth>,
    modifier: Modifier = Modifier
) {
    if (data.size < 2) return

    val sorted = data.sortedBy { it.date }
    val hoursValues = sorted.map { it.sleepHours ?: 0f }
    val maxHours = (
        hoursValues.maxOrNull()?.let {
            kotlin.math.ceil(it).toInt().coerceAtLeast(1)
        } ?: HealthConstants.DEFAULT_SLEEP_CHART_MAX_HOURS_BASE
        ) + 1
    val density = LocalDensity.current
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val goodColor = Color(0xFF4CAF50)
    val warnColor = Color(0xFFFFC107)
    val badColor = Color(0xFFFF5252)

    Column(modifier = modifier.fillMaxWidth()) {
        val layoutDirection = LocalLayoutDirection.current
        val isRtl = layoutDirection == LayoutDirection.Rtl
        val dateFormat = stringResource(R.string.date_format_month_day)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(start = 40.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
        ) {
            val chartWidth = size.width
            val chartHeight = size.height
            val stepX = if (sorted.size > 1) chartWidth / (sorted.size - 1) else chartWidth

            // Helper function to calculate X coordinate with RTL support
            fun getX(index: Int): Float {
                return if (isRtl) chartWidth - (stepX * index) else stepX * index
            }
            val textPaint = android.graphics.Paint().apply {
                color = textColor.hashCode()
                textSize = with(density) { 10.dp.toPx() }
                textAlign = android.graphics.Paint.Align.LEFT
            }

            // Y-axis labels (hours)
            for (i in 0..maxHours step 2) {
                val y = chartHeight - (i.toFloat() / maxHours * chartHeight)
                drawContext.canvas.nativeCanvas.drawText(
                    "$i",
                    0f,
                    y + 4.dp.toPx(),
                    textPaint
                )
                // Grid line
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1f
                )
            }

            // X-axis labels (dates)
            val formatter = DateTimeFormatter.ofPattern(dateFormat)
            sorted.forEachIndexed { index, day ->
                val x = getX(index)
                val label = try {
                    LocalDate.parse(day.date).format(formatter)
                } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (_: Exception) { "" }
                // Adjust text position for RTL
                val textX = if (isRtl) x + 16f else x - 16f
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    textX,
                    chartHeight + 16.dp.toPx(),
                    textPaint
                )
            }

            // Sleep hours line
            val hoursPath = Path().apply {
                sorted.forEachIndexed { index, day ->
                    val x = getX(index)
                    val y = chartHeight - ((day.sleepHours ?: 0f) / maxHours * chartHeight)
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
            }
            drawPath(
                path = hoursPath,
                color = primaryColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Sleep hours dots
            sorted.forEachIndexed { index, day ->
                val x = getX(index)
                val y = chartHeight - ((day.sleepHours ?: 0f) / maxHours * chartHeight)
                drawCircle(
                    color = primaryColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }

            // Quality bars (as colored dots below the line)
            sorted.forEachIndexed { index, day ->
                val x = getX(index)
                val quality = day.sleepQuality ?: 0
                if (quality > 0) {
                    val barHeight = (quality.toFloat() / HealthConstants.SLEEP_QUALITY_MAX) * 20.dp.toPx()
                    val barColor = when {
                        quality >= 4 -> goodColor
                        quality >= HealthConstants.SLEEP_QUALITY_FAIR_THRESHOLD -> warnColor
                        else -> badColor
                    }
                    drawLine(
                        color = barColor.copy(alpha = 0.6f),
                        start = Offset(x, chartHeight),
                        end = Offset(x, chartHeight - barHeight),
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Legend
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.sleep_trend_legend,
                sorted.firstOrNull()?.date?.take(7) ?: "",
                sorted.lastOrNull()?.date?.take(7) ?: ""
            ),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun SleepCycleCorrelationCard(
    follicularAvgSleep: Float?,
    lutealAvgSleep: Float?,
    modifier: Modifier = Modifier
) {
    if (follicularAvgSleep == null || lutealAvgSleep == null) return

    val diff = lutealAvgSleep - follicularAvgSleep
    val isBetter = diff > 0.3f
    val isWorse = diff < -0.3f

    if (!isBetter && !isWorse) return

    androidx.compose.material3.Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isWorse) {
                    stringResource(
                        R.string.sleep_luteal_worse
                    )
                } else {
                    stringResource(R.string.sleep_luteal_better)
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isWorse) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isWorse) {
                    stringResource(R.string.sleep_luteal_comparison_worse, lutealAvgSleep, follicularAvgSleep, -diff)
                } else {
                    stringResource(R.string.sleep_luteal_comparison_better, lutealAvgSleep, follicularAvgSleep, diff)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
