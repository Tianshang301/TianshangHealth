package com.tianshang.health.navigation

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.database.entity.DailySteps
import com.tianshang.health.feature.fitness.util.CalorieCalculator
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun WeeklyStepsChart(steps: List<DailySteps>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.steps_weekly_trend),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            val maxSteps = steps.maxOfOrNull { it.count } ?: 1
            steps.forEach { dailySteps ->
                val date = LocalDate.parse(dailySteps.date)
                val dayName = date.format(DateTimeFormatter.ofPattern("EEE"))
                val barWeight = dailySteps.count.toFloat() / maxSteps
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(0.2f)
                    )
                    Box(modifier = Modifier.weight(0.6f).height(16.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = barWeight)
                                .height(16.dp)
                                .padding(end = if (barWeight < 1f) 4.dp else 0.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }
                    Text(
                        text = "${dailySteps.count}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(0.2f)
                    )
                }
            }
        }
    }
}

@Composable
internal fun EnergySummaryCard(
    todaySteps: Int,
    todayStepsCalories: Float,
    todayWorkoutCalories: Float,
    todayCombinedCalories: Float,
    weeklySteps: Int,
    weeklyStepsCalories: Float,
    weeklyWorkoutCalories: Float,
    weeklyWorkoutCount: Int,
    weeklyDurationMinutes: Int
) {
    val weeklyCombinedCalories = weeklyStepsCalories + weeklyWorkoutCalories

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.energy_summary_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.energy_today),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EnergyRow(
                        label = stringResource(R.string.energy_steps),
                        value = formatNumber(todaySteps),
                        kcal = todayStepsCalories
                    )
                    EnergyRow(
                        label = stringResource(R.string.energy_workouts),
                        value = stringResource(R.string.unit_kcal_format, todayWorkoutCalories),
                        kcal = null
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    EnergyRow(
                        label = stringResource(R.string.energy_total),
                        value = stringResource(R.string.unit_kcal_format, todayCombinedCalories),
                        kcal = null,
                        isBold = true
                    )
                    Text(
                        text = stringResource(
                            R.string.unit_kj_format,
                            CalorieCalculator.kcalToKj(todayCombinedCalories)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.energy_this_week),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EnergyRow(
                        label = stringResource(R.string.energy_steps),
                        value = formatNumber(weeklySteps),
                        kcal = weeklyStepsCalories
                    )
                    EnergyRow(
                        label = stringResource(R.string.energy_workouts),
                        value = stringResource(R.string.unit_kcal_format, weeklyWorkoutCalories),
                        kcal = null
                    )
                    Text(
                        text = stringResource(
                            R.string.energy_workouts_summary,
                            weeklyWorkoutCount,
                            weeklyDurationMinutes
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    EnergyRow(
                        label = stringResource(R.string.energy_total),
                        value = stringResource(R.string.unit_kcal_format, weeklyCombinedCalories),
                        kcal = null,
                        isBold = true
                    )
                    Text(
                        text = stringResource(
                            R.string.unit_kj_format,
                            CalorieCalculator.kcalToKj(weeklyCombinedCalories)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EnergyRow(
    label: String,
    value: String,
    kcal: Float?,
    isBold: Boolean = false
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = if (isBold) {
                MaterialTheme.typography.titleSmall
            } else {
                MaterialTheme.typography.bodyMedium
            },
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        if (kcal != null) {
            Text(
                text = stringResource(R.string.unit_kcal_format, kcal),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
private fun formatNumber(value: Int): String {
    val tenThousandsFormat = stringResource(R.string.number_ten_thousands_format)
    return if (value >= HealthConstants.STEPS_FORMAT_TEN_THOUSANDS_THRESHOLD) {
        String.format(tenThousandsFormat, value / HealthConstants.STEPS_FORMAT_TEN_THOUSANDS_THRESHOLD.toDouble())
    } else if (value >= 1000) {
        String.format("%.1fk", value / 1000.0)
    } else {
        "$value"
    }
}
