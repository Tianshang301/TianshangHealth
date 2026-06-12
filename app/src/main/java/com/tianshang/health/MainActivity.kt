package com.tianshang.health

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.tianshang.health.core.security.auth.AppLockManager
import com.tianshang.health.core.security.auth.ScreenshotProtectionManager
import com.tianshang.health.core.security.encryption.KeystoreManager
import com.tianshang.health.navigation.MainNavigation
import com.tianshang.health.ui.theme.TianshangHealthTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val langCode = try {
            val prefs = KeystoreManager.getEncryptedSharedPreferences(newBase)
            prefs.getString("app_language", "zh") ?: "zh"
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

        val prefs = KeystoreManager.getEncryptedSharedPreferences(this)
        val useCustomTheme = prefs.getBoolean("use_custom_theme", false)

        setContent {
            val customHue by remember {
                mutableStateOf(
                    if (useCustomTheme) prefs.getFloat("theme_hue", 340f) else null
                )
            }
            val customSat by remember {
                mutableStateOf(
                    if (useCustomTheme) prefs.getFloat("theme_saturation", 0.6f) else null
                )
            }
            val customLit by remember {
                mutableStateOf(
                    if (useCustomTheme) prefs.getFloat("theme_lightness", 0.7f) else null
                )
            }

            TianshangHealthTheme(
                customHue = customHue,
                customSaturation = customSat,
                customLightness = customLit
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
    }
}
