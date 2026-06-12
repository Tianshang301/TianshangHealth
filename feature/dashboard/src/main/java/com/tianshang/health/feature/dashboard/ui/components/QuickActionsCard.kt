package com.tianshang.health.feature.dashboard.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tianshang.health.core.common.R

@Composable
fun QuickActionsCard(
    showPeriodAction: Boolean,
    onRecordPeriod: () -> Unit,
    onRecordSteps: () -> Unit,
    onRecordHealth: () -> Unit,
    onRecordNutrition: () -> Unit,
    onRecordSleep: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showPeriodAction) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    label = stringResource(R.string.dashboard_record_period),
                    icon = {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onClick = onRecordPeriod,
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    label = stringResource(R.string.dashboard_record_steps),
                    icon = {
                        Icon(
                            Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onClick = onRecordSteps,
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    label = stringResource(R.string.nav_sleep),
                    icon = { Icon(Icons.Default.Bedtime, contentDescription = null, modifier = Modifier.size(24.dp)) },
                    onClick = onRecordSleep,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    label = stringResource(R.string.dashboard_record_health),
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(24.dp)) },
                    onClick = onRecordHealth,
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    label = stringResource(R.string.nav_nutrition),
                    icon = { Icon(Icons.Default.Fastfood, contentDescription = null, modifier = Modifier.size(24.dp)) },
                    onClick = onRecordNutrition,
                    modifier = Modifier.weight(1f)
                )
                Box(modifier = Modifier.weight(1f))
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    label = stringResource(R.string.dashboard_record_steps),
                    icon = {
                        Icon(
                            Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onClick = onRecordSteps,
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    label = stringResource(R.string.dashboard_record_health),
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(24.dp)) },
                    onClick = onRecordHealth,
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    label = stringResource(R.string.nav_nutrition),
                    icon = { Icon(Icons.Default.Fastfood, contentDescription = null, modifier = Modifier.size(24.dp)) },
                    onClick = onRecordNutrition,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
