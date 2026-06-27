@file:Suppress("MatchingDeclarationName")

package com.tianshang.health.core.common.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class EdgePosition { Top, Bottom }

@Composable
fun ScrollEdgeEffect(
    position: EdgePosition,
    height: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color(0xFF1C1B1F) else Color(0xFFFFFBFE)

    val gradientColors = when (position) {
        EdgePosition.Top -> listOf(
            backgroundColor,
            backgroundColor.copy(alpha = 0f)
        )
        EdgePosition.Bottom -> listOf(
            backgroundColor.copy(alpha = 0f),
            backgroundColor
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.verticalGradient(colors = gradientColors)
            )
    )
}
