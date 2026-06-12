package com.tianshang.health.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.tianshang.health.core.common.ui.theme.HslColorEngine

@Composable
fun TianshangHealthTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    customHue: Float? = null,
    customSaturation: Float? = null,
    customLightness: Float? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        customHue != null && customSaturation != null && customLightness != null -> {
            val schemes = HslColorEngine.generateColorScheme(customHue, customSaturation, customLightness)
            if (darkTheme) schemes.dark else schemes.light
        }
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> {
            val schemes = HslColorEngine.generateColorScheme(340f, 0.6f, 0.7f)
            if (darkTheme) schemes.dark else schemes.light
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
