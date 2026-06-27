@file:Suppress("MagicNumber", "UnnecessaryParentheses")

package com.tianshang.health.core.common.ui.glass

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AdaptiveTint {

    @Composable
    fun tintFor(
        baseColor: Color,
        isDark: Boolean = isSystemInDarkTheme()
    ): Color {
        val hsl = colorToHsl(baseColor)
        return if (isDark) {
            hslToColor(hsl.hue, (hsl.saturation * 0.7f).coerceIn(0f, 1f), (hsl.lightness * 0.8f).coerceIn(0f, 1f))
        } else {
            hslToColor(hsl.hue, (hsl.saturation * 0.5f).coerceIn(0f, 1f), (hsl.lightness * 1.2f).coerceIn(0f, 1f))
        }
    }

    @Composable
    fun tintedGlass(
        tintColor: Color,
        alpha: Float = 0.12f,
        isDark: Boolean = isSystemInDarkTheme()
    ): Color {
        return tintFor(tintColor, isDark).copy(alpha = alpha)
    }

    private data class Hsl(val hue: Float, val saturation: Float, val lightness: Float)

    private fun colorToHsl(color: Color): Hsl {
        val r = color.red
        val g = color.green
        val b = color.blue
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min

        val hue = when {
            delta == 0f -> 0f
            max == r -> 60f * (((g - b) / delta) % 6f)
            max == g -> 60f * (((b - r) / delta) + 2f)
            else -> 60f * (((r - g) / delta) + 4f)
        }.let { if (it < 0) it + 360f else it }

        val lightness = (max + min) / 2f
        val saturation = if (delta == 0f) 0f else delta / (1f - kotlin.math.abs(2f * lightness - 1f))

        return Hsl(hue, saturation.coerceIn(0f, 1f), lightness.coerceIn(0f, 1f))
    }

    private fun hslToColor(hue: Float, saturation: Float, lightness: Float): Color {
        val c = (1f - kotlin.math.abs(2f * lightness - 1f)) * saturation
        val x = c * (1f - kotlin.math.abs((hue / 60f) % 2f - 1f))
        val m = lightness - c / 2f

        val (r, g, b) = when {
            hue < 60f -> Triple(c, x, 0f)
            hue < 120f -> Triple(x, c, 0f)
            hue < 180f -> Triple(0f, c, x)
            hue < 240f -> Triple(0f, x, c)
            hue < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        return Color((r + m).coerceIn(0f, 1f), (g + m).coerceIn(0f, 1f), (b + m).coerceIn(0f, 1f))
    }
}
