package com.tianshang.health.feature.analysis.util

import android.content.Context
import android.content.res.Configuration
import com.tianshang.health.core.security.encryption.KeystoreManager
import java.util.Locale

object LocalizedContextProvider {

    private const val PREF_LANGUAGE_KEY = "app_language"
    private const val DEFAULT_LANGUAGE = "zh"

    private val localeMapping = mapOf(
        "zh" to "zh",
        "en" to "en",
        "ja" to "ja",
        "ko" to "ko",
        "fr" to "fr",
        "es" to "es",
        "de" to "de",
        "ru" to "ru",
        "it" to "it",
        "tr" to "tr",
        "hi" to "hi",
        "th" to "th",
        "vi" to "vi",
        "id" to "id",
        "ms" to "ms",
        "pl" to "pl",
        "nl" to "nl",
        "sv" to "sv",
        "uk" to "uk",
        "pt" to "pt",
        "ar" to "ar"
    )

    fun getLocalizedContext(baseContext: Context): Context {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(baseContext)
        val langCode = prefs.getString(PREF_LANGUAGE_KEY, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
        val localeTag = localeMapping[langCode] ?: DEFAULT_LANGUAGE
        val locale = Locale.forLanguageTag(localeTag)

        Locale.setDefault(locale)
        val config = Configuration(baseContext.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return baseContext.createConfigurationContext(config)
    }
}
