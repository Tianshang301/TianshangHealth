package com.tianshang.health.feature.period.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.ui.theme.PainMild
import com.tianshang.health.core.common.ui.theme.PainModerate
import com.tianshang.health.core.common.ui.theme.PainNone
import com.tianshang.health.core.common.ui.theme.PainSevere
import com.tianshang.health.core.database.entity.PeriodRecord
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun CycleLengthChart(
    records: List<PeriodRecord>,
    modifier: Modifier = Modifier
) {
    val cycleLengths = calculateCycleLengths(records)

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.chart_title_cycle_length_trend),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (cycleLengths.isEmpty()) {
                Text(
                    text = stringResource(R.string.chart_no_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val maxLength = cycleLengths.maxOrNull() ?: 1
                val minLength = cycleLengths.minOrNull() ?: 1

                cycleLengths.takeLast(6).forEachIndexed { index, length ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.chart_cycle_label, index + 1),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(0.3f)
                        )

                        val barWeight = (length - minLength + 1).toFloat() / (maxLength - minLength + 1)
                        Box(
                            modifier = Modifier
                                .weight(0.5f)
                                .height(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = barWeight)
                                    .height(16.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        }

                        Text(
                            text = stringResource(R.string.chart_cycle_length_label, length),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(0.2f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val avgLength = cycleLengths.average().toInt()
                Text(
                    text = stringResource(R.string.stats_avg_format, avgLength),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PainTrendChart(
    records: List<PeriodRecord>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.chart_title_pain_trend),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            val recordsWithPain = records.filter { it.painLevel != null }.takeLast(6)

            if (recordsWithPain.isEmpty()) {
                Text(
                    text = stringResource(R.string.chart_no_pain_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                recordsWithPain.forEach { record ->
                    val date = LocalDate.parse(record.startDate)
                    val painColor = when (record.painLevel) {
                        0 -> PainNone
                        1 -> PainMild
                        2 -> PainModerate
                        3 -> PainSevere
                        else -> Color.Gray
                    }
                    val painLabel = when (record.painLevel) {
                        0 -> stringResource(R.string.pain_none)
                        1 -> stringResource(R.string.pain_mild)
                        2 -> stringResource(R.string.pain_moderate)
                        3 -> stringResource(R.string.pain_severe)
                        else -> stringResource(R.string.chart_pain_unknown)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$date",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = painLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = painColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticsSummary(
    records: List<PeriodRecord>,
    modifier: Modifier = Modifier
) {
    val cycleLengths = calculateCycleLengths(records)
    val periodLengths = calculatePeriodLengths(records)

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.cycle_statistics),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatRow(stringResource(R.string.stats_total_records), "${records.size}")
            StatRow(stringResource(R.string.stats_total_cycles), "${cycleLengths.size}")

            if (cycleLengths.isNotEmpty()) {
                val avgCycle = cycleLengths.average().toInt()
                StatRow(stringResource(R.string.stats_avg_cycle), stringResource(R.string.stats_days_suffix, avgCycle))
            }

            if (periodLengths.isNotEmpty()) {
                val avgPeriod = periodLengths.average().toInt()
                StatRow(
                    stringResource(R.string.stats_avg_period),
                    stringResource(R.string.stats_days_suffix, avgPeriod)
                )
            }

            if (cycleLengths.size >= 3) {
                val regularity = calculateRegularity(cycleLengths)
                val regularityText = when (regularity) {
                    "Regular" -> stringResource(R.string.regularity_regular)
                    "Somewhat regular" -> stringResource(R.string.regularity_somewhat_regular)
                    "Irregular" -> stringResource(R.string.regularity_irregular)
                    else -> stringResource(R.string.regularity_insufficient_data)
                }
                StatRow(stringResource(R.string.stats_regularity_text), regularityText)
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun calculateCycleLengths(records: List<PeriodRecord>): List<Int> {
    val sortedRecords = records.sortedBy { it.startDate }
    val lengths = mutableListOf<Int>()

    for (i in 1 until sortedRecords.size) {
        val prev = LocalDate.parse(sortedRecords[i - 1].startDate)
        val curr = LocalDate.parse(sortedRecords[i].startDate)
        val days = ChronoUnit.DAYS.between(prev, curr).toInt()
        if (days in 21..45) {
            lengths.add(days)
        }
    }

    return lengths
}

private fun calculatePeriodLengths(records: List<PeriodRecord>): List<Int> {
    return records.mapNotNull { record ->
        if (record.endDate != null) {
            val start = LocalDate.parse(record.startDate)
            val end = LocalDate.parse(record.endDate)
            val days = ChronoUnit.DAYS.between(start, end).toInt() + 1
            if (days in 2..10) days else null
        } else {
            null
        }
    }
}

private fun calculateRegularity(cycleLengths: List<Int>): String {
    if (cycleLengths.size < 3) return "Insufficient data"

    val stdDev = calculateStandardDeviation(cycleLengths)
    val cv = stdDev / cycleLengths.average()

    return when {
        cv < 0.1 -> "Regular"
        cv < 0.2 -> "Somewhat regular"
        else -> "Irregular"
    }
}

private fun calculateStandardDeviation(values: List<Int>): Double {
    val mean = values.average()
    val variance = values.map { (it - mean) * (it - mean) }.average()
    return Math.sqrt(variance)
}
