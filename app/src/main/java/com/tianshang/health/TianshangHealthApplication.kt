package com.tianshang.health

import android.app.Application
import android.os.Debug
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.tianshang.health.core.security.encryption.KeystoreManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TianshangHealthApplication : Application(), Configuration.Provider {

    companion object {
        private const val KEY_APP_LANGUAGE = "l4m8n2"
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        checkDebuggerConnected()
        applySavedLanguage()
    }

    private fun checkDebuggerConnected() {
        if (Debug.isDebuggerConnected()) {
            // In a real scenario, you might want to exit the app or disable sensitive features
            // For now, we just log a warning since this is a health app that shouldn't crash
            android.util.Log.w("TianshangHealth", "Debugger detected - sensitive operations may be at risk")
        }
    }

    private fun applySavedLanguage() {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(this)
        val langCode = prefs.getString(KEY_APP_LANGUAGE, "zh") ?: "zh"
        val localeTag = mapOf(
            "zh" to "zh", "en" to "en", "ja" to "ja", "ko" to "ko",
            "fr" to "fr", "es" to "es", "de" to "de", "ru" to "ru",
            "it" to "it", "tr" to "tr", "hi" to "hi", "th" to "th",
            "vi" to "vi", "id" to "id", "ms" to "ms", "pl" to "pl",
            "nl" to "nl", "sv" to "sv", "uk" to "uk", "pt" to "pt",
            "ar" to "ar"
        )[langCode] ?: "zh"
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(localeTag)
        )
    }
}
