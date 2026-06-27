package com.tianshang.health.core.common.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeConfigHolder {
    private val _customHue = MutableStateFlow<Float?>(null)
    val customHue: StateFlow<Float?> = _customHue.asStateFlow()

    private val _customSaturation = MutableStateFlow<Float?>(null)
    val customSaturation: StateFlow<Float?> = _customSaturation.asStateFlow()

    private val _customLightness = MutableStateFlow<Float?>(null)
    val customLightness: StateFlow<Float?> = _customLightness.asStateFlow()

    fun update(hue: Float?, saturation: Float?, lightness: Float?) {
        _customHue.value = hue
        _customSaturation.value = saturation
        _customLightness.value = lightness
    }
}
