package com.tianshang.health.feature.analysis.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tianshang.health.core.common.R
import com.tianshang.health.feature.analysis.domain.AnalysisUiState
import com.tianshang.health.feature.analysis.ui.charts.DonutChart
import com.tianshang.health.feature.analysis.ui.charts.DonutSlice
import com.tianshang.health.feature.analysis.ui.charts.SimpleBarChart
import com.tianshang.health.feature.analysis.ui.components.SuggestionCard
import com.tianshang.health.feature.analysis.viewmodel.AnalysisViewModel

@Composable
fun AnalysisScreen(
    onNavigateToReport: () -> Unit = {},
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is AnalysisUiState.Loading -> LoadingContent()
        is AnalysisUiState.Success -> AnalysisContent(state.data, state.isFemale, onNavigateToReport)
        is AnalysisUiState.Error -> ErrorContent(state.message, onRetry = { viewModel.refresh() })
    }
}

@Composable
private fun AnalysisContent(
    data: com.tianshang.health.feature.analysis.domain.AnalysisData,
    isFemale: Boolean,
    onNavigateToReport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.analysis_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (data.suggestions.isNotEmpty()) {
            SectionHeader(icon = Icons.Default.Lightbulb, title = stringResource(R.string.analysis_section_suggestions))
            Spacer(modifier = Modifier.height(8.dp))
            data.suggestions.forEach { suggestion ->
                SuggestionCard(suggestion = suggestion)
                Spacer(modifier = Modifier.height(6.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        data.nutrition?.let { nutrition ->
            SectionHeader(icon = Icons.Default.LocalDining, title = stringResource(R.string.analysis_section_nutrition))
            Spacer(modifier = Modifier.height(8.dp))
            NutritionSection(nutrition)
            Spacer(modifier = Modifier.height(16.dp))
        }

        data.calorieBalance?.let { balance ->
            SectionHeader(
                icon = Icons.Default.LocalFireDepartment,
                title = stringResource(R.string.analysis_section_calorie_balance)
            )
            Spacer(modifier = Modifier.height(8.dp))
            CalorieBalanceSection(balance)
            Spacer(modifier = Modifier.height(16.dp))
        }

        data.exercise?.let { exercise ->
            SectionHeader(
                icon = Icons.Default.DirectionsRun,
                title = stringResource(R.string.analysis_section_exercise)
            )
            Spacer(modifier = Modifier.height(8.dp))
            ExerciseSection(exercise)
            Spacer(modifier = Modifier.height(16.dp))
        }

        data.sleep?.let { sleep ->
            SectionHeader(icon = Icons.Default.Bedtime, title = stringResource(R.string.analysis_section_sleep))
            Spacer(modifier = Modifier.height(8.dp))
            SleepSection(sleep)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (data.phaseComparisons.isNotEmpty() && isFemale) {
            SectionHeader(
                icon = Icons.Default.EnergySavingsLeaf,
                title = stringResource(R.string.analysis_section_phase)
            )
            Spacer(modifier = Modifier.height(8.dp))
            PhaseSection(data.phaseComparisons)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (data.nutrition == null && data.sleep == null && data.exercise == null) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.analysis_no_data),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateToReport,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Analytics, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.export_report))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun NutritionSection(nutrition: com.tianshang.health.feature.analysis.domain.WeeklyNutrition) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem(
                    stringResource(R.string.analysis_avg_daily_calories),
                    stringResource(R.string.unit_kcal_format, nutrition.avgCalories),
                    Modifier.weight(1f)
                )
                StatItem(
                    stringResource(R.string.analysis_protein),
                    stringResource(R.string.unit_grams_format, nutrition.avgProteinGrams),
                    Modifier.weight(1f)
                )
                StatItem(
                    stringResource(R.string.analysis_carbs),
                    stringResource(R.string.unit_grams_format, nutrition.avgCarbsGrams),
                    Modifier.weight(1f)
                )
                StatItem(
                    stringResource(R.string.analysis_fat),
                    stringResource(R.string.unit_grams_format, nutrition.avgFatGrams),
                    Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.analysis_nutrient_ratio),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            DonutChart(
                slices = listOf(
                    DonutSlice(stringResource(R.string.analysis_protein), nutrition.avgProteinGrams, Color(0xFF43A047)),
                    DonutSlice(stringResource(R.string.analysis_carbs), nutrition.avgCarbsGrams, Color(0xFFFB8C00)),
                    DonutSlice(stringResource(R.string.analysis_fat), nutrition.avgFatGrams, Color(0xFF1E88E5))
                ),
                centerLabel = stringResource(R.string.unit_kcal_format, nutrition.avgCalories)
            )

            if (nutrition.avgWaterMl > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.analysis_avg_water_format, nutrition.avgWaterMl),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CalorieBalanceSection(balance: com.tianshang.health.feature.analysis.domain.CalorieBalance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem(
                    stringResource(R.string.analysis_avg_intake),
                    stringResource(R.string.unit_kcal_format, balance.avgCaloriesIn),
                    Modifier.weight(1f)
                )
                StatItem(
                    stringResource(R.string.analysis_exercise_burned),
                    stringResource(R.string.unit_kcal_format, balance.avgCaloriesBurned),
                    Modifier.weight(1f)
                )
                StatItem(
                    stringResource(R.string.nutrition_resting_energy),
                    stringResource(R.string.unit_kcal_format, balance.avgRestingEnergy),
                    Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem(
                    label = stringResource(R.string.nutrition_total_expenditure),
                    value = stringResource(R.string.unit_kcal_format, balance.avgTotalExpenditure),
                    modifier = Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.primary
                )
                val net = balance.avgCaloriesIn - balance.avgTotalExpenditure
                val isSurplus = net > 0
                StatItem(
                    label = stringResource(R.string.nutrition_calorie_balance),
                    value = stringResource(R.string.unit_net_kcal_format, net),
                    modifier = Modifier.weight(1f),
                    valueColor = if (isSurplus) Color(0xFFE53935) else Color(0xFF43A047)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.analysis_daily_net_calories),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            SimpleBarChart(
                data = balance.netDaily.map {
                    com.tianshang.health.feature.analysis.domain.DailyValue(
                        it.first,
                        it.second
                    )
                },
                color = Color(0xFFE53935)
            )
        }
    }
}

@Composable
private fun ExerciseSection(exercise: com.tianshang.health.feature.analysis.domain.WeeklyExercise) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem(
                    stringResource(R.string.analysis_weekly_total_minutes),
                    stringResource(R.string.unit_minutes_int_format, exercise.totalMinutes),
                    Modifier.weight(1f)
                )
                StatItem(
                    stringResource(R.string.analysis_daily_avg_minutes),
                    stringResource(R.string.unit_minutes_float_format, exercise.avgMinutesPerDay),
                    Modifier.weight(1f)
                )
                StatItem(
                    stringResource(R.string.analysis_calories_burned),
                    stringResource(R.string.unit_kcal_format, exercise.totalCaloriesBurned),
                    Modifier.weight(1f)
                )
            }

            if (exercise.typeDistribution.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.analysis_exercise_type_distribution),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                DonutChart(
                    slices = exercise.typeDistribution.mapIndexed { index, typeValue ->
                        val colors = listOf(
                            Color(0xFF43A047),
                            Color(0xFF1E88E5),
                            Color(0xFFFB8C00),
                            Color(0xFF8E24AA),
                            Color(0xFFE53935)
                        )
                        DonutSlice(typeValue.type, typeValue.value, colors[index % colors.size])
                    },
                    centerLabel = stringResource(R.string.unit_minutes_int_format, exercise.totalMinutes)
                )
            }
        }
    }
}

@Composable
private fun SleepSection(sleep: com.tianshang.health.feature.analysis.domain.WeeklySleep) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem(
                    stringResource(R.string.analysis_avg_sleep_hours),
                    stringResource(R.string.unit_hours_format, sleep.avgHours),
                    Modifier.weight(1f)
                )
                StatItem(
                    stringResource(R.string.sleep_deep_hours),
                    stringResource(R.string.unit_hours_format, sleep.avgDeepHours),
                    Modifier.weight(1f)
                )
                StatItem(
                    stringResource(R.string.analysis_quality_score),
                    stringResource(R.string.unit_sleep_quality_format, sleep.avgQuality),
                    Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.analysis_daily_sleep_hours),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            SimpleBarChart(
                data = sleep.dailyHours,
                color = Color(0xFF5C6BC0)
            )
        }
    }
}

@Composable
private fun PhaseSection(comparisons: List<com.tianshang.health.feature.analysis.domain.PhaseComparison>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            comparisons.forEach { phase ->
                Text(
                    text = stringResource(phase.phaseNameResId),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatItem(
                        stringResource(R.string.nav_sleep),
                        stringResource(R.string.unit_hours_format, phase.sleepAvg),
                        Modifier.weight(1f)
                    )
                    StatItem(stringResource(R.string.steps_label), "%.0f".format(phase.stepsAvg), Modifier.weight(1f))
                    StatItem(stringResource(R.string.analysis_mood), "%.1f".format(phase.moodAvg), Modifier.weight(1f))
                    StatItem(
                        stringResource(R.string.analysis_stress),
                        "%.1f".format(phase.stressAvg),
                        Modifier.weight(1f)
                    )
                    StatItem(
                        stringResource(R.string.nutrition_calories),
                        "%.0f".format(phase.calorieAvg),
                        Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified
) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (valueColor != androidx.compose.ui.graphics.Color.Unspecified) valueColor else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Button(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}
