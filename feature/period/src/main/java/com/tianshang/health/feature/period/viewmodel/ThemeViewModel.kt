package com.tianshang.health.feature.period.viewmodel

import androidx.lifecycle.ViewModel
import com.tianshang.health.core.security.encryption.KeystoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    @ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val prefs = KeystoreManager.getEncryptedSharedPreferences(context)

    private val _hue = MutableStateFlow(prefs.getFloat("theme_hue", 340f))
    val hue: StateFlow<Float> = _hue.asStateFlow()

    private val _saturation = MutableStateFlow(prefs.getFloat("theme_saturation", 0.6f))
    val saturation: StateFlow<Float> = _saturation.asStateFlow()

    private val _lightness = MutableStateFlow(prefs.getFloat("theme_lightness", 0.7f))
    val lightness: StateFlow<Float> = _lightness.asStateFlow()

    private val _useCustomTheme = MutableStateFlow(prefs.getBoolean("use_custom_theme", false))
    val useCustomTheme: StateFlow<Boolean> = _useCustomTheme.asStateFlow()

    fun setHue(value: Float) {
        _hue.value = value
        prefs.edit().putFloat("theme_hue", value).apply()
    }

    fun setSaturation(value: Float) {
        _saturation.value = value
        prefs.edit().putFloat("theme_saturation", value).apply()
    }

    fun setLightness(value: Float) {
        _lightness.value = value
        prefs.edit().putFloat("theme_lightness", value).apply()
    }

    fun applyPreset(hue: Float, saturation: Float, lightness: Float) {
        setHue(hue)
        setSaturation(saturation)
        setLightness(lightness)
        prefs.edit().putBoolean("use_custom_theme", true).apply()
        _useCustomTheme.value = true
    }

    fun toggleCustomTheme(enabled: Boolean) {
        prefs.edit().putBoolean("use_custom_theme", enabled).apply()
        _useCustomTheme.value = enabled
    }
}
