package com.tianshang.health.feature.period.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.ui.theme.FertileGreen
import com.tianshang.health.core.common.ui.theme.OvulationBlue
import com.tianshang.health.core.common.ui.theme.PeriodRed

@Composable
fun CycleStatusCard(
    currentCycleDay: Int?,
    daysUntilNextPeriod: Int?,
    isPeriodActive: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.cycle_status_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Current cycle day
                CycleInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = if (isPeriodActive) PeriodRed else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    value = currentCycleDay?.toString() ?: "--",
                    label = stringResource(R.string.cycle_day_label)
                )

                // Days until next period
                CycleInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = OvulationBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    value = daysUntilNextPeriod?.toString() ?: "--",
                    label = stringResource(R.string.days_until_label)
                )
            }

            if (isPeriodActive) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.period_active_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PeriodRed,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CycleInfoItem(
    icon: @Composable () -> Unit,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PredictionCard(
    nextPeriodDate: String?,
    ovulationDate: String?,
    fertileWindow: String?,
    cycleLength: Int?,
    confidence: String?,
    cycleCount: Int,
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
                text = stringResource(R.string.predictions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            PredictionRow(
                label = stringResource(R.string.next_period_label),
                value = nextPeriodDate ?: stringResource(R.string.not_enough_data),
                color = PeriodRed
            )

            PredictionRow(
                label = stringResource(R.string.ovulation),
                value = ovulationDate ?: stringResource(R.string.not_enough_data),
                color = OvulationBlue
            )

            PredictionRow(
                label = stringResource(R.string.fertile_window),
                value = fertileWindow ?: stringResource(R.string.not_enough_data),
                color = FertileGreen
            )

            if (cycleLength != null) {
                PredictionRow(
                    label = stringResource(R.string.cycle_length_label),
                    value = stringResource(R.string.stats_days_suffix, cycleLength),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (confidence != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.confidence_label, confidence, cycleCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PredictionRow(
    label: String,
    value: String,
    color: Color
) {
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
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
