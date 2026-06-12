package com.tianshang.health.feature.nutrition.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.tianshang.health.feature.nutrition.data.repository.CycleNutritionRecommendation

@Composable
fun CycleNutritionCard(
    recommendation: CycleNutritionRecommendation
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
    val phaseLabel = when (recommendation.currentPhase) {
        CyclePhase.MENSTRUAL -> stringResource(R.string.phase_menstrual)
        CyclePhase.FOLLICULAR -> stringResource(R.string.phase_follicular)
        CyclePhase.OVULATORY -> stringResource(R.string.phase_ovulatory)
        CyclePhase.LUTEAL -> stringResource(R.string.phase_luteal)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = phaseColor)
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
                    text = stringResource(R.string.nutrition_cycle_guidance),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = onPhaseColor
                )
                Text(
                    text = "$phaseLabel ${stringResource(R.string.phase_day_range, recommendation.phaseDayRange)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = onPhaseColor.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(recommendation.generalAdviceResId),
                style = MaterialTheme.typography.bodySmall,
                color = onPhaseColor
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.nutrition_focus_nutrients),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = onPhaseColor
                    )
                    Text(
                        text = stringResource(recommendation.focusNutrientsResId),
                        style = MaterialTheme.typography.bodySmall,
                        color = onPhaseColor.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.nutrition_recommended_foods),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = onPhaseColor
                    )
                    Text(
                        text = stringResource(recommendation.recommendedFoodsResId),
                        style = MaterialTheme.typography.bodySmall,
                        color = onPhaseColor.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.nutrition_limit_foods),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = onPhaseColor
                    )
                    Text(
                        text = stringResource(recommendation.foodsToLimitResId),
                        style = MaterialTheme.typography.bodySmall,
                        color = onPhaseColor.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}
