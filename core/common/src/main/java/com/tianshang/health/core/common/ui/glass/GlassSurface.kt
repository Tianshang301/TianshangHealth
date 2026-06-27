package com.tianshang.health.core.common.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.glass(
    variant: GlassVariant = GlassVariant.Regular,
    cornerRadius: Dp = 28.dp,
    elevation: Dp = 2.dp,
    isDark: Boolean = false,
): Modifier {
    val glassColors = if (isDark) GlassTheme.darkColors else GlassTheme.lightColors
    val shape = RoundedCornerShape(cornerRadius)

    val surfaceStart = when (variant) {
        GlassVariant.Regular -> glassColors.surfaceStart
        GlassVariant.Clear -> glassColors.surfaceClearStart
    }
    val surfaceEnd = when (variant) {
        GlassVariant.Regular -> glassColors.surfaceEnd
        GlassVariant.Clear -> glassColors.surfaceClearEnd
    }

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = glassColors.shadow,
            spotColor = glassColors.shadow,
        )
        .clip(shape)
        .background(
            brush = Brush.linearGradient(
                colors = listOf(surfaceStart, surfaceEnd)
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(glassColors.borderOuter, glassColors.borderInner)
            ),
            shape = shape
        )
}

fun Modifier.glassHighlight(
    cornerRadius: Dp = 28.dp,
    isDark: Boolean = false,
): Modifier {
    val highlightColor = if (isDark) {
        Color.White.copy(alpha = 0.04f)
    } else {
        Color.White.copy(alpha = 0.30f)
    }

    return this.drawBehind {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(highlightColor, Color.Transparent),
                startY = 0f,
                endY = size.height * 0.4f
            )
        )
    }
}
