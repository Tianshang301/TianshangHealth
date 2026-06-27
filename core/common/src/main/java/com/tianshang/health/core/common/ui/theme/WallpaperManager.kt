package com.tianshang.health.core.common.ui.theme

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

object WallpaperManager {

    enum class WallpaperType(val value: Int) {
        NONE(0),
        PRESET(1),
        GRADIENT(2),
        IMAGE(3);

        companion object {
            fun fromValue(value: Int): WallpaperType =
                entries.find { it.value == value } ?: NONE
        }
    }

    enum class GradientType {
        LINEAR, RADIAL, SWEEP
    }

    data class WallpaperConfig(
        val type: WallpaperType = WallpaperType.NONE,
        val presetIndex: Int = -1,
        val gradientStartColor: Color = Color(0xFFB3E5FC),
        val gradientEndColor: Color = Color(0xFFE1BEE7),
        val gradientAngle: Float = 45f,
        val imageUri: String = "",
        val opacity: Float = 0.12f,
        val blurRadius: Float = 0f
    )

    data class WallpaperPreset(
        val nameResId: Int,
        val gradientType: GradientType,
        val startColor: Color,
        val endColor: Color,
        val angle: Float = 45f
    )

    val PRESETS = listOf(
        WallpaperPreset(
            nameResId = com.tianshang.health.core.common.R.string.wallpaper_preset_soft_gradient,
            gradientType = GradientType.LINEAR,
            startColor = Color(0xFFF5E6E9),
            endColor = Color(0xFFE8EAF6),
            angle = 45f
        ),
        WallpaperPreset(
            nameResId = com.tianshang.health.core.common.R.string.wallpaper_preset_warm_glow,
            gradientType = GradientType.LINEAR,
            startColor = Color(0xFFFFF3E0),
            endColor = Color(0xFFFFE0B2),
            angle = 45f
        ),
        WallpaperPreset(
            nameResId = com.tianshang.health.core.common.R.string.wallpaper_preset_cool_breeze,
            gradientType = GradientType.LINEAR,
            startColor = Color(0xFFE3F2FD),
            endColor = Color(0xFFE8F5E9),
            angle = 135f
        ),
        WallpaperPreset(
            nameResId = com.tianshang.health.core.common.R.string.wallpaper_preset_nature_leaves,
            gradientType = GradientType.LINEAR,
            startColor = Color(0xFFF1F8E9),
            endColor = Color(0xFFE0F2F1),
            angle = 0f
        ),
        WallpaperPreset(
            nameResId = com.tianshang.health.core.common.R.string.wallpaper_preset_geometric,
            gradientType = GradientType.SWEEP,
            startColor = Color(0xFFF3E5F5),
            endColor = Color(0xFFE1BEE7),
            angle = 0f
        ),
        WallpaperPreset(
            nameResId = com.tianshang.health.core.common.R.string.wallpaper_preset_starry,
            gradientType = GradientType.LINEAR,
            startColor = Color(0xFFE8EAF6),
            endColor = Color(0xFFF3E5F5),
            angle = 315f
        )
    )

    fun createPresetBrush(preset: WallpaperPreset): Brush {
        val angleRad = preset.angle * kotlin.math.PI / 180f
        val cosA = kotlin.math.cos(angleRad).toFloat()
        val sinA = kotlin.math.sin(angleRad).toFloat()
        return when (preset.gradientType) {
            GradientType.LINEAR -> Brush.linearGradient(
                colors = listOf(preset.startColor, preset.endColor),
                start = Offset(0.5f * (1f - cosA + sinA), 0.5f * (1f - sinA - cosA)),
                end = Offset(0.5f * (1f + cosA - sinA), 0.5f * (1f + sinA + cosA))
            )
            GradientType.RADIAL -> Brush.radialGradient(
                colors = listOf(preset.startColor, preset.endColor)
            )
            GradientType.SWEEP -> Brush.sweepGradient(
                colors = listOf(preset.startColor, preset.endColor)
            )
        }
    }

    private const val PREFS_FILE = "w9p2r5"
    private const val PREFS_KEY_TYPE = "w1t3"
    private const val PREFS_KEY_PRESET = "w2p4"
    private const val PREFS_KEY_GRADIENT_START = "w3g5"
    private const val PREFS_KEY_GRADIENT_END = "w4g6"
    private const val PREFS_KEY_GRADIENT_ANGLE = "w5a7"
    private const val PREFS_KEY_IMAGE_URI = "w6i8"
    private const val PREFS_KEY_OPACITY = "w7o9"
    private const val PREFS_KEY_BLUR = "w8b1"
    private const val PREFS_KEY_ENABLED = "w9e2"

    private val _configFlow = MutableStateFlow(WallpaperConfig())
    val configFlow: StateFlow<WallpaperConfig> = _configFlow.asStateFlow()

    private fun updateConfigFlow(context: Context) {
        _configFlow.value = loadConfig(context)
    }

    fun saveConfig(context: Context, config: WallpaperConfig) {
        val prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt(PREFS_KEY_TYPE, config.type.value)
            putInt(PREFS_KEY_PRESET, config.presetIndex)
            putInt(PREFS_KEY_GRADIENT_START, config.gradientStartColor.toArgb())
            putInt(PREFS_KEY_GRADIENT_END, config.gradientEndColor.toArgb())
            putFloat(PREFS_KEY_GRADIENT_ANGLE, config.gradientAngle)
            putString(PREFS_KEY_IMAGE_URI, config.imageUri)
            putFloat(PREFS_KEY_OPACITY, config.opacity)
            putFloat(PREFS_KEY_BLUR, config.blurRadius)
            putBoolean(PREFS_KEY_ENABLED, config.type != WallpaperType.NONE)
        }.apply()
        _configFlow.value = config
    }

    fun initialize(context: Context) {
        _configFlow.value = loadConfig(context)
    }

    fun loadConfig(context: Context): WallpaperConfig {
        val prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean(PREFS_KEY_ENABLED, false)
        if (!enabled) return WallpaperConfig()

        return WallpaperConfig(
            type = WallpaperType.fromValue(prefs.getInt(PREFS_KEY_TYPE, 0)),
            presetIndex = prefs.getInt(PREFS_KEY_PRESET, -1),
            gradientStartColor = Color(prefs.getInt(PREFS_KEY_GRADIENT_START, 0xFFB3E5FC.toInt())),
            gradientEndColor = Color(prefs.getInt(PREFS_KEY_GRADIENT_END, 0xFFE1BEE7.toInt())),
            gradientAngle = prefs.getFloat(PREFS_KEY_GRADIENT_ANGLE, 45f),
            imageUri = prefs.getString(PREFS_KEY_IMAGE_URI, "") ?: "",
            opacity = prefs.getFloat(PREFS_KEY_OPACITY, 0.12f),
            blurRadius = prefs.getFloat(PREFS_KEY_BLUR, 0f)
        )
    }

    fun copyImageToInternal(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val dir = File(context.filesDir, "wallpapers")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, "custom_wallpaper_${System.currentTimeMillis()}.jpg")
                file.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                file.absolutePath
            }
        } catch (_: Exception) {
            null
        }
    }

    fun loadBitmap(path: String): android.graphics.Bitmap? {
        return try {
            BitmapFactory.decodeFile(path)
        } catch (_: Exception) {
            null
        }
    }
}
