package com.tianshang.health.core.common.ui.glass

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GlassChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    val isDark = isSystemInDarkTheme()
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier.glass(
            variant = if (selected) GlassVariant.Regular else GlassVariant.Clear,
            cornerRadius = 20.dp,
            elevation = if (selected) 2.dp else 0.dp,
            isDark = isDark
        ),
        leadingIcon = leadingIcon,
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = Color.Transparent,
            selectedBorderColor = Color.Transparent,
            enabled = true,
            selected = selected,
        ),
    )
}
