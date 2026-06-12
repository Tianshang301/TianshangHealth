package com.tianshang.health.feature.fitness.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.constants.CyclePhase
import com.tianshang.health.feature.fitness.domain.CycleFitnessRecommendation
import com.tianshang.health.feature.fitness.util.ExerciseType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CycleFitnessRecommendationCard(
    recommendation: CycleFitnessRecommendation
) {
    val phaseColor = when (recommendation.currentPhase) {
        CyclePhase.MENSTRUAL -> MaterialTheme.colorScheme.errorContainer
        CyclePhase.FOLLICULAR -> MaterialTheme.colorScheme.primaryContainer
        CyclePhase.OVULATORY -> MaterialTheme.colorScheme.tertiaryContainer
        CyclePhase.LUTEAL -> MaterialTheme.colorScheme.secondaryContainer
    }
    val onPhaseColor = when (recommendation.currentPhase) {
        CyclePhase.MENSTRUAL -> MaterialTheme.colorScheme.onErrorContainer
        CyclePhase.FOLLICULAR -> MaterialTheme.colorScheme.onPrimaryContainer
        CyclePhase.OVULATORY -> MaterialTheme.colorScheme.onTertiaryContainer
        CyclePhase.LUTEAL -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = phaseColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = onPhaseColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(phaseLabelResId(recommendation.currentPhase)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = onPhaseColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.phase_day_range, recommendation.phaseDayRange),
                    style = MaterialTheme.typography.bodySmall,
                    color = onPhaseColor.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(fitnessAdviceResId(recommendation.adviceKey)),
                style = MaterialTheme.typography.bodyMedium,
                color = onPhaseColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.fitness_recommended),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = onPhaseColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                recommendation.recommendedExerciseTypes.forEach { typeValue ->
                    val exerciseType = ExerciseType.fromValue(typeValue)
                    FilterChip(
                        selected = true,
                        onClick = {},
                        label = {
                            Text(
                                text = stringResource(exerciseType.displayNameResId),
                                maxLines = 1
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            if (recommendation.avoidExerciseTypes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.fitness_avoid),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = onPhaseColor.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    recommendation.avoidExerciseTypes.forEach { typeValue ->
                        val exerciseType = ExerciseType.fromValue(typeValue)
                        FilterChip(
                            selected = true,
                            onClick = {},
                            label = {
                                Text(
                                    text = if (typeValue == "overtraining") {
                                        stringResource(R.string.fitness_avoid_overtraining)
                                    } else {
                                        stringResource(exerciseType.displayNameResId)
                                    },
                                    maxLines = 1
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.error,
                                selectedLabelColor = MaterialTheme.colorScheme.onError
                            )
                        )
                    }
                }
            }
        }
    }
}

@StringRes
private fun phaseLabelResId(phase: CyclePhase): Int = when (phase) {
    CyclePhase.MENSTRUAL -> R.string.phase_menstrual_full
    CyclePhase.FOLLICULAR -> R.string.phase_follicular_full
    CyclePhase.OVULATORY -> R.string.phase_ovulatory_full
    CyclePhase.LUTEAL -> R.string.phase_luteal_full
}

@StringRes
private fun fitnessAdviceResId(adviceKey: String): Int = when (adviceKey) {
    "menstrual" -> R.string.fitness_advice_menstrual
    "follicular" -> R.string.fitness_advice_follicular
    "ovulatory" -> R.string.fitness_advice_ovulatory
    "luteal" -> R.string.fitness_advice_luteal
    else -> R.string.fitness_advice_menstrual
}
