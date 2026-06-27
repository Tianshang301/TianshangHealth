package com.tianshang.health.feature.analysis.ui.components

import androidx.compose.foundation.layout.Column
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
import com.tianshang.health.core.common.ui.glass.GlassCard
import com.tianshang.health.core.common.ui.glass.GlassVariant
import com.tianshang.health.feature.analysis.domain.Suggestion

@Composable
fun SuggestionCard(
    suggestion: Suggestion,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (suggestion) {
        is Suggestion.Warning -> Icons.Default.Warning to Color(0xFFE53935)
        is Suggestion.Tip -> Icons.Default.Info to Color(0xFF1E88E5)
        is Suggestion.Positive -> Icons.Default.CheckCircle to Color(0xFF43A047)
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        variant = GlassVariant.Clear,
        cornerRadius = 20.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = stringResource(suggestion.titleResId),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
                Spacer(modifier = Modifier.height(2.dp))
                val descText = if (suggestion.formatArgs.isNotEmpty()) {
                    stringResource(suggestion.descriptionResId, *suggestion.formatArgs.toTypedArray())
                } else {
                    stringResource(suggestion.descriptionResId)
                }
                Text(
                    text = descText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
