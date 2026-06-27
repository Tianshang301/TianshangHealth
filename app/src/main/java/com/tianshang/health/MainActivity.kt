package com.tianshang.health

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.tianshang.health.core.common.ui.theme.ThemeConfigHolder
import com.tianshang.health.core.common.ui.theme.WallpaperManager
import com.tianshang.health.core.security.auth.AppLockManager
import com.tianshang.health.core.security.auth.ScreenshotProtectionManager
import com.tianshang.health.core.security.encryption.KeystoreManager
import com.tianshang.health.navigation.MainNavigation
import com.tianshang.health.ui.theme.TianshangHealthTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val KEY_APP_LANGUAGE = "l4m8n2"
        private const val KEY_THEME_HUE = "h2u4e6"
        private const val KEY_THEME_SATURATION = "s1a3t5"
        private const val KEY_THEME_LIGHTNESS = "l7i9g2"
        private const val KEY_USE_CUSTOM_THEME = "c8u2s4"
    }

    override fun attachBaseContext(newBase: Context) {
        val langCode = try {
            val prefs = KeystoreManager.getEncryptedSharedPreferences(newBase)
            prefs.getString(KEY_APP_LANGUAGE, "zh") ?: "zh"
        } catch (_: Exception) {
            "zh"
        }
        val locale = Locale.forLanguageTag(
            when (langCode) {
                "zh" -> "zh-Hans"
                else -> langCode
            }
        )
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        AppLockManager.init(this)
        ScreenshotProtectionManager.init(this)
        ScreenshotProtectionManager.registerActivity(this)

        ProcessLifecycleOwner.get().lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_STOP -> AppLockManager.onBackground()
                    Lifecycle.Event.ON_START -> AppLockManager.onForeground()
                    else -> {}
                }
            }
        )

        WallpaperManager.initialize(this)

        val prefs = KeystoreManager.getEncryptedSharedPreferences(this)
        val useCustomTheme = prefs.getBoolean(KEY_USE_CUSTOM_THEME, false)
        if (useCustomTheme) {
            ThemeConfigHolder.update(
                prefs.getFloat(KEY_THEME_HUE, 340f),
                prefs.getFloat(KEY_THEME_SATURATION, 0.6f),
                prefs.getFloat(KEY_THEME_LIGHTNESS, 0.7f)
            )
        }

        setContent {
            val customHue by ThemeConfigHolder.customHue.collectAsState()
            val customSat by ThemeConfigHolder.customSaturation.collectAsState()
            val customLit by ThemeConfigHolder.customLightness.collectAsState()
            val wallpaperConfig by WallpaperManager.configFlow.collectAsState()

            TianshangHealthTheme(
                customHue = customHue,
                customSaturation = customSat,
                customLightness = customLit,
                wallpaperConfig = wallpaperConfig
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(wallpaperConfig = wallpaperConfig)
                }
            }
        }
    }
}
