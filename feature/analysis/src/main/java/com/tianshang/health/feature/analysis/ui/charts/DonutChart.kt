package com.tianshang.health.feature.analysis.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tianshang.health.core.common.R
import com.tianshang.health.feature.analysis.domain.DailyValue

data class DonutSlice(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    centerLabel: String,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(120.dp)) {
                val strokeWidth = 28f
                val radius = (size.minDimension - strokeWidth) / 2
                val topLeft = Offset(
                    (size.width - radius * 2 - strokeWidth) / 2,
                    (size.height - radius * 2 - strokeWidth) / 2
                )
                val arcSize = Size(radius * 2 + strokeWidth, radius * 2 + strokeWidth)

                var startAngle = -90f
                slices.forEach { slice ->
                    val sweepAngle = if (total > 0) (slice.value / total) * 360f else 0f
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth)
                    )
                    startAngle += sweepAngle
                }
            }
            Text(
                text = centerLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            slices.forEach { slice ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .padding(end = 6.dp)
                    ) {
                        Canvas(modifier = Modifier.size(10.dp)) {
                            drawCircle(color = slice.color)
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    val percentage = if (total > 0) (slice.value / total * 100).toInt() else 0
                    Text(
                        text = stringResource(R.string.chart_donut_legend_format, slice.label, percentage),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleBarChart(
    data: List<DailyValue>,
    color: Color,
    modifier: Modifier = Modifier
) {
    val maxVal = data.maxOfOrNull { it.value } ?: 1f
    if (maxVal <= 0f) return

    Column(modifier = modifier.fillMaxWidth()) {
        data.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(40.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (item.value / maxVal).coerceIn(0f, 1f))
                            .height(12.dp)
                    ) {
                        Canvas(modifier = Modifier.size(12.dp)) {
                            drawRoundRect(
                                color = color.copy(alpha = 0.7f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (item.value == item.value.toInt().toFloat() && item.value < 100) {
                        "${item.value.toInt()}"
                    } else {
                        "%.0f".format(item.value)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(36.dp)
                )
            }
        }
    }
}
