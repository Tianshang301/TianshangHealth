package com.tianshang.health.feature.dashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.ui.glass.GlassCard
import com.tianshang.health.core.common.ui.glass.GlassVariant

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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ActionCardVertical(
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                label = stringResource(R.string.dashboard_record_steps),
                circleColor = Color(0xFFE0F2F9),
                iconTint = Color(0xFF0B5F7E),
                onClick = onRecordSteps,
                modifier = Modifier.weight(1f)
            )
            ActionCardVertical(
                icon = Icons.Default.Bedtime,
                label = stringResource(R.string.nav_sleep),
                circleColor = Color(0xFFE8EEF8),
                iconTint = Color(0xFF2C3E6B),
                onClick = onRecordSleep,
                modifier = Modifier.weight(1f)
            )
            if (showPeriodAction) {
                ActionCardVertical(
                    icon = Icons.Default.Fastfood,
                    label = stringResource(R.string.nav_nutrition),
                    circleColor = Color(0xFFFEF3E2),
                    iconTint = Color(0xFFD35400),
                    onClick = onRecordNutrition,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (showPeriodAction) {
                ActionCardHorizontal(
                    icon = Icons.Default.DateRange,
                    label = stringResource(R.string.dashboard_record_period),
                    circleColor = Color(0xFFF5E4F0),
                    iconTint = Color(0xFF8E44AD),
                    onClick = onRecordPeriod,
                    modifier = Modifier.weight(1f)
                )
                ActionCardHorizontal(
                    icon = Icons.Default.Favorite,
                    label = stringResource(R.string.dashboard_record_health),
                    circleColor = Color(0xFFFDE2E2),
                    iconTint = Color(0xFFC0392B),
                    onClick = onRecordHealth,
                    modifier = Modifier.weight(1f)
                )
            } else {
                ActionCardHorizontal(
                    icon = Icons.Default.Fastfood,
                    label = stringResource(R.string.nav_nutrition),
                    circleColor = Color(0xFFFEF3E2),
                    iconTint = Color(0xFFD35400),
                    onClick = onRecordNutrition,
                    modifier = Modifier.weight(1f)
                )
                ActionCardHorizontal(
                    icon = Icons.Default.Favorite,
                    label = stringResource(R.string.dashboard_record_health),
                    circleColor = Color(0xFFFDE2E2),
                    iconTint = Color(0xFFC0392B),
                    onClick = onRecordHealth,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ActionCardVertical(
    icon: ImageVector,
    label: String,
    circleColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.clickable { onClick() },
        variant = GlassVariant.Clear,
        cornerRadius = 20.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(circleColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ActionCardHorizontal(
    icon: ImageVector,
    label: String,
    circleColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.clickable { onClick() },
        variant = GlassVariant.Clear,
        cornerRadius = 20.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(circleColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(21.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}
