package com.tianshang.health.core.common.ui.glass

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class GlassColors(
    val surfaceStart: Color,
    val surfaceEnd: Color,
    val surfaceClearStart: Color,
    val surfaceClearEnd: Color,
    val borderOuter: Color,
    val borderInner: Color,
    val shadow: Color,
    val highlight: Color,
)

object GlassTheme {

    val lightColors = GlassColors(
        surfaceStart = Color(0xCCFFFFFF),
        surfaceEnd = Color(0xB3F5F5F5),
        surfaceClearStart = Color(0x40FFFFFF),
        surfaceClearEnd = Color(0x33F5F5F5),
        borderOuter = Color.White.copy(alpha = 0.18f),
        borderInner = Color.White.copy(alpha = 0.06f),
        shadow = Color.Black.copy(alpha = 0.08f),
        highlight = Color.White.copy(alpha = 0.50f),
    )

    val darkColors = GlassColors(
        surfaceStart = Color(0xCC2A2A2E),
        surfaceEnd = Color(0xB31C1B1F),
        surfaceClearStart = Color(0x402A2A2E),
        surfaceClearEnd = Color(0x331C1B1F),
        borderOuter = Color.White.copy(alpha = 0.06f),
        borderInner = Color.White.copy(alpha = 0.02f),
        shadow = Color.Black.copy(alpha = 0.20f),
        highlight = Color.White.copy(alpha = 0.12f),
    )

    @Composable
    fun colors(): GlassColors {
        return if (isSystemInDarkTheme()) darkColors else lightColors
    }
}
