package com.tianshang.health.core.common.ui.theme

import androidx.annotation.StringRes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.tianshang.health.core.common.R

object HslColorEngine {

    data class Hsl(val hue: Float, val saturation: Float, val lightness: Float)

    fun fromColor(color: Color): Hsl {
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

    fun toColor(hsl: Hsl): Color {
        val (h, s, l) = hsl
        val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
        val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
        val m = l - c / 2f

        val (r, g, b) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        return Color((r + m), (g + m), (b + m))
    }

    fun toColor(hue: Float, saturation: Float, lightness: Float): Color {
        return toColor(Hsl(hue, saturation, lightness))
    }

    fun generateColorScheme(hue: Float, saturation: Float, lightness: Float): SchemePair {
        val primary = toColor(hue, saturation, lightness)
        val secondary =
            toColor((hue + 30f) % 360f, (saturation * 0.7f).coerceIn(0f, 1f), (lightness * 0.85f).coerceIn(0f, 1f))
        val tertiary =
            toColor((hue + 60f) % 360f, (saturation * 0.5f).coerceIn(0f, 1f), (lightness * 0.7f).coerceIn(0f, 1f))

        val light = lightColorScheme(
            primary = primary,
            onPrimary = if (lightness > 0.5f) Color.Black else Color.White,
            primaryContainer = primary.copy(alpha = 0.2f),
            onPrimaryContainer = primary,
            secondary = secondary,
            onSecondary = if (lightness > 0.5f) Color.Black else Color.White,
            secondaryContainer = secondary.copy(alpha = 0.2f),
            onSecondaryContainer = secondary,
            tertiary = tertiary,
            onTertiary = Color.Black,
            tertiaryContainer = tertiary.copy(alpha = 0.2f),
            onTertiaryContainer = tertiary,
            background = Color(0xFFFFFBFE),
            onBackground = Color(0xFF1C1B1F),
            surface = Color(0xFFFFFBFE),
            onSurface = Color(0xFF1C1B1F),
            surfaceVariant = Color(0xFFE7E0EC),
            onSurfaceVariant = Color(0xFF49454F),
            outline = Color(0xFF79747E)
        )

        val darkPrimary = toColor(hue, (saturation * 0.8f).coerceIn(0f, 1f), (lightness * 0.6f).coerceIn(0f, 1f))

        val dark = darkColorScheme(
            primary = darkPrimary,
            onPrimary = Color.Black,
            primaryContainer = darkPrimary.copy(alpha = 0.2f),
            onPrimaryContainer = darkPrimary,
            secondary = secondary,
            onSecondary = Color.Black,
            secondaryContainer = secondary.copy(alpha = 0.2f),
            onSecondaryContainer = secondary,
            tertiary = tertiary,
            onTertiary = Color.Black,
            tertiaryContainer = tertiary.copy(alpha = 0.2f),
            onTertiaryContainer = tertiary,
            background = Color(0xFF1C1B1F),
            onBackground = Color(0xFFE6E1E5),
            surface = Color(0xFF1C1B1F),
            onSurface = Color(0xFFE6E1E5),
            surfaceVariant = Color(0xFF49454F),
            onSurfaceVariant = Color(0xFFCAC4D0),
            outline = Color(0xFF938F99)
        )

        return SchemePair(light, dark)
    }

    data class GlassScheme(
        val lightSurfaceStart: Color,
        val lightSurfaceEnd: Color,
        val darkSurfaceStart: Color,
        val darkSurfaceEnd: Color,
        val tintHighlight: Color,
    )

    fun generateGlassScheme(hue: Float, saturation: Float, lightness: Float): GlassScheme {
        val tintHighlight = toColor(hue, saturation, lightness)
        return GlassScheme(
            lightSurfaceStart = Color(0xCCFFFFFF),
            lightSurfaceEnd = Color(0xB3F5F5F5),
            darkSurfaceStart = Color(0xCC2A2A2E),
            darkSurfaceEnd = Color(0xB31C1B1F),
            tintHighlight = tintHighlight,
        )
    }

    data class SchemePair(val light: androidx.compose.material3.ColorScheme, val dark: androidx.compose.material3.ColorScheme)

    val PRESETS = listOf(
        Preset(R.string.color_pink, 340f, 0.6f, 0.7f),
        Preset(R.string.color_blue, 210f, 0.6f, 0.55f),
        Preset(R.string.color_green, 120f, 0.5f, 0.5f),
        Preset(R.string.color_purple, 270f, 0.5f, 0.6f),
        Preset(R.string.color_orange, 30f, 0.7f, 0.6f),
        Preset(R.string.color_teal, 180f, 0.5f, 0.5f)
    )

    data class Preset(@StringRes val labelResId: Int, val hue: Float, val saturation: Float, val lightness: Float)
}
