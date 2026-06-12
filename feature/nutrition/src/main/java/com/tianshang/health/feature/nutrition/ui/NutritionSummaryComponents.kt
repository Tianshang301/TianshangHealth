package com.tianshang.health.feature.nutrition.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.feature.nutrition.viewmodel.NutritionState

@Composable
fun DailyNutritionSummary(
    state: NutritionState,
    onAddWater: (Float) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.nutrition_daily_summary),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${state.mealCountToday} ${stringResource(R.string.nutrition_meals)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            NutrientBar(
                label = stringResource(R.string.nutrition_calories),
                value = stringResource(R.string.unit_kcal_format, state.dailyCalories),
                progress = (state.dailyCalories / HealthConstants.DEFAULT_CALORIE_GOAL).coerceIn(0f, 1f),
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))

            NutrientBar(
                label = stringResource(R.string.nutrition_protein),
                value = stringResource(R.string.unit_grams_decimal_format, state.dailyProtein),
                progress = (state.dailyProtein / HealthConstants.DEFAULT_PROTEIN_GOAL_G).coerceIn(0f, 1f),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            NutrientBar(
                label = stringResource(R.string.nutrition_carbs),
                value = stringResource(R.string.unit_grams_decimal_format, state.dailyCarbs),
                progress = (state.dailyCarbs / HealthConstants.DEFAULT_CARBS_GOAL_G).coerceIn(0f, 1f),
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(8.dp))

            NutrientBar(
                label = stringResource(R.string.nutrition_fat),
                value = stringResource(R.string.unit_grams_decimal_format, state.dailyFat),
                progress = (state.dailyFat / HealthConstants.DEFAULT_FAT_GOAL_G).coerceIn(0f, 1f),
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))
            WaterIntakeCard(
                currentMl = state.dailyWaterIntake,
                goalMl = state.waterIntakeGoal,
                onAddWater = onAddWater
            )

            Spacer(modifier = Modifier.height(16.dp))
            EnergyBalanceSection(state = state)
        }
    }
}

@Composable
fun NutrientBar(
    label: String,
    value: String,
    progress: Float,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun WaterIntakeCard(
    currentMl: Float,
    goalMl: Float,
    onAddWater: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = stringResource(R.string.nutrition_water),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = stringResource(R.string.nutrition_water_format, currentMl.toInt(), goalMl.toInt()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (currentMl / goalMl).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(100f, 250f, 500f).forEach { ml ->
                OutlinedButton(
                    onClick = { onAddWater(ml) },
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = stringResource(R.string.nutrition_add_water, ml.toInt()),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun EnergyBalanceSection(state: NutritionState) {
    val balance = state.dailyCalories - state.totalExpenditure
    val isSurplus = balance > 0
    val balanceColor = if (isSurplus) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.tertiary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.nutrition_energy_balance),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EnergyItem(
                    label = stringResource(R.string.nutrition_resting_energy),
                    value = stringResource(R.string.unit_kcal_format, state.restingEnergy),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                EnergyItem(
                    label = stringResource(R.string.nutrition_exercise_energy),
                    value = stringResource(R.string.unit_kcal_format, state.exerciseCalories),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                EnergyItem(
                    label = stringResource(R.string.nutrition_total_expenditure),
                    value = stringResource(R.string.unit_kcal_format, state.totalExpenditure),
                    color = MaterialTheme.colorScheme.primary,
                    isBold = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.nutrition_calorie_balance),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(R.string.unit_net_kcal_format, balance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = balanceColor
                )
            }
        }
    }
}

@Composable
fun EnergyItem(
    label: String,
    value: String,
    color: Color,
    isBold: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
