package com.tianshang.health.feature.period.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.util.NumberFormatUtils
import com.tianshang.health.core.database.entity.DailySteps

@Composable
fun MaleHealthPlaceholder(
    todaySteps: DailySteps?,
    recentSteps: List<DailySteps>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.male_health_trend),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.male_health_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        StepsOverviewCard(
            todaySteps = todaySteps,
            recentSteps = recentSteps
        )
    }
}

@Composable
private fun StepsOverviewCard(
    todaySteps: DailySteps?,
    recentSteps: List<DailySteps>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.steps_overview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = stringResource(R.string.steps_today),
                    value = NumberFormatUtils.formatCompactNumber(todaySteps?.count ?: 0),
                    goal = todaySteps?.goal?.let { "/ ${NumberFormatUtils.formatCompactNumber(it)}" }
                )
                StatItem(
                    label = stringResource(R.string.steps_weekly_avg_label),
                    value = if (recentSteps.isNotEmpty()) {
                        NumberFormatUtils.formatCompactNumber(recentSteps.map { it.count }.average().toInt())
                    } else {
                        "--"
                    }
                )
                StatItem(
                    label = stringResource(R.string.steps_record_days),
                    value = "${recentSteps.count { it.count > 0 }}"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (recentSteps.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.steps_weekly_trend_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                SimpleBarChart(
                    data = recentSteps.takeLast(7).map { it.count },
                    labels = recentSteps.takeLast(7).map { it.date.takeLast(5) }
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    goal: String? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (goal != null) {
            Text(
                text = goal,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SimpleBarChart(
    data: List<Int>,
    labels: List<String>
) {
    if (data.isEmpty()) return

    val maxVal = data.max().coerceAtLeast(1)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        data.forEachIndexed { index, value ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = labels.getOrElse(index) { "" },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(48.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val fraction = value.toFloat() / maxVal
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp)
                        .padding(end = 8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                Text(
                    text = NumberFormatUtils.formatCompactNumber(value),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(40.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
