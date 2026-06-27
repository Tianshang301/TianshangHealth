package com.tianshang.health.feature.period.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.ui.theme.ThemeConfigHolder
import com.tianshang.health.core.common.ui.theme.WallpaperManager
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.security.encryption.KeystoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val stringResolver: StringResolver
) : ViewModel() {

    companion object {
        private const val KEY_THEME_HUE = "h2u4e6"
        private const val KEY_THEME_SATURATION = "s1a3t5"
        private const val KEY_THEME_LIGHTNESS = "l7i9g2"
        private const val KEY_USE_CUSTOM_THEME = "c8u2s4"
    }

    private val prefs = KeystoreManager.getEncryptedSharedPreferences(context)

    private val _hue = MutableStateFlow(prefs.getFloat(KEY_THEME_HUE, 340f))
    val hue: StateFlow<Float> = _hue.asStateFlow()

    private val _saturation = MutableStateFlow(prefs.getFloat(KEY_THEME_SATURATION, 0.6f))
    val saturation: StateFlow<Float> = _saturation.asStateFlow()

    private val _lightness = MutableStateFlow(prefs.getFloat(KEY_THEME_LIGHTNESS, 0.7f))
    val lightness: StateFlow<Float> = _lightness.asStateFlow()

    private val _useCustomTheme = MutableStateFlow(prefs.getBoolean(KEY_USE_CUSTOM_THEME, false))
    val useCustomTheme: StateFlow<Boolean> = _useCustomTheme.asStateFlow()

    private val _wallpaperConfig = MutableStateFlow(WallpaperManager.loadConfig(context))
    val wallpaperConfig: StateFlow<WallpaperManager.WallpaperConfig> = _wallpaperConfig.asStateFlow()

    private val _saveError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val saveError: SharedFlow<String> = _saveError.asSharedFlow()

    private fun syncThemeConfig() {
        val enabled = _useCustomTheme.value
        ThemeConfigHolder.update(
            hue = if (enabled) _hue.value else null,
            saturation = if (enabled) _saturation.value else null,
            lightness = if (enabled) _lightness.value else null
        )
    }

    fun setHue(value: Float) {
        _hue.value = value
        prefs.edit().putFloat(KEY_THEME_HUE, value).apply()
        syncThemeConfig()
    }

    fun setSaturation(value: Float) {
        _saturation.value = value
        prefs.edit().putFloat(KEY_THEME_SATURATION, value).apply()
        syncThemeConfig()
    }

    fun setLightness(value: Float) {
        _lightness.value = value
        prefs.edit().putFloat(KEY_THEME_LIGHTNESS, value).apply()
        syncThemeConfig()
    }

    fun applyPreset(hue: Float, saturation: Float, lightness: Float) {
        setHue(hue)
        setSaturation(saturation)
        setLightness(lightness)
        prefs.edit().putBoolean(KEY_USE_CUSTOM_THEME, true).apply()
        _useCustomTheme.value = true
        syncThemeConfig()
    }

    fun toggleCustomTheme(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_USE_CUSTOM_THEME, enabled).apply()
        _useCustomTheme.value = enabled
        syncThemeConfig()
    }

    fun setWallpaperType(type: WallpaperManager.WallpaperType) {
        val config = _wallpaperConfig.value.copy(
            type = type,
            presetIndex = if (type == WallpaperManager.WallpaperType.PRESET) 0 else _wallpaperConfig.value.presetIndex
        )
        _wallpaperConfig.value = config
        WallpaperManager.saveConfig(context, config)
    }

    fun setWallpaperPreset(presetIndex: Int) {
        val config = _wallpaperConfig.value.copy(
            type = WallpaperManager.WallpaperType.PRESET,
            presetIndex = presetIndex
        )
        _wallpaperConfig.value = config
        WallpaperManager.saveConfig(context, config)
    }

    fun setGradientStartColor(color: androidx.compose.ui.graphics.Color) {
        val config = _wallpaperConfig.value.copy(
            type = WallpaperManager.WallpaperType.GRADIENT,
            gradientStartColor = color
        )
        _wallpaperConfig.value = config
        WallpaperManager.saveConfig(context, config)
    }

    fun setGradientEndColor(color: androidx.compose.ui.graphics.Color) {
        val config = _wallpaperConfig.value.copy(
            type = WallpaperManager.WallpaperType.GRADIENT,
            gradientEndColor = color
        )
        _wallpaperConfig.value = config
        WallpaperManager.saveConfig(context, config)
    }

    fun setGradientAngle(angle: Float) {
        val config = _wallpaperConfig.value.copy(
            type = WallpaperManager.WallpaperType.GRADIENT,
            gradientAngle = angle
        )
        _wallpaperConfig.value = config
        WallpaperManager.saveConfig(context, config)
    }

    fun setWallpaperImage(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val savedPath = if (uri.scheme == "file") {
                uri.path
            } else {
                WallpaperManager.copyImageToInternal(context, uri)
            }
            if (savedPath != null) {
                val config = _wallpaperConfig.value.copy(
                    type = WallpaperManager.WallpaperType.IMAGE,
                    imageUri = savedPath
                )
                _wallpaperConfig.value = config
                WallpaperManager.saveConfig(context, config)
            } else {
                _saveError.tryEmit(stringResolver.getString(R.string.error_failed_save_wallpaper))
            }
        }
    }

    fun setWallpaperOpacity(opacity: Float) {
        val config = _wallpaperConfig.value.copy(opacity = opacity)
        _wallpaperConfig.value = config
        WallpaperManager.saveConfig(context, config)
    }

    fun removeWallpaper() {
        val config = WallpaperManager.WallpaperConfig()
        _wallpaperConfig.value = config
        WallpaperManager.saveConfig(context, config)
    }
}
