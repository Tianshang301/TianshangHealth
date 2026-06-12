package com.tianshang.health.feature.period.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tianshang.health.core.security.encryption.KeystoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LanguageOption(
    val code: String,
    val displayName: String,
    val nativeName: String
)

@HiltViewModel
class LanguageViewModel @Inject constructor(
    @ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val prefs = KeystoreManager.getEncryptedSharedPreferences(context)

    private val _selectedLanguage = MutableStateFlow(
        prefs.getString("app_language", "zh") ?: "zh"
    )
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _languageApplied = Channel<Unit>(Channel.CONFLATED)
    val languageApplied = _languageApplied.receiveAsFlow()

    val languages = listOf(
        LanguageOption("zh", "Chinese", "中文"),
        LanguageOption("en", "English", "English"),
        LanguageOption("ja", "Japanese", "日本語"),
        LanguageOption("ko", "Korean", "한국어"),
        LanguageOption("fr", "French", "Français"),
        LanguageOption("es", "Spanish", "Español"),
        LanguageOption("de", "German", "Deutsch"),
        LanguageOption("ru", "Russian", "Русский"),
        LanguageOption("it", "Italian", "Italiano"),
        LanguageOption("tr", "Turkish", "Türkçe"),
        LanguageOption("hi", "Hindi", "हिन्दी"),
        LanguageOption("th", "Thai", "ไทย"),
        LanguageOption("vi", "Vietnamese", "Tiếng Việt"),
        LanguageOption("id", "Indonesian", "Bahasa Indonesia"),
        LanguageOption("ms", "Malay", "Bahasa Melayu"),
        LanguageOption("pl", "Polish", "Polski"),
        LanguageOption("nl", "Dutch", "Nederlands"),
        LanguageOption("sv", "Swedish", "Svenska"),
        LanguageOption("uk", "Ukrainian", "Українська"),
        LanguageOption("pt", "Portuguese", "Português"),
        LanguageOption("ar", "Arabic", "العربية"),
    )

    fun setLanguage(languageCode: String) {
        _selectedLanguage.value = languageCode
        prefs.edit().putString("app_language", languageCode).apply()

        val localeTag = mapOf(
            "zh" to "zh", "en" to "en", "ja" to "ja", "ko" to "ko",
            "fr" to "fr", "es" to "es", "de" to "de", "ru" to "ru",
            "it" to "it", "tr" to "tr", "hi" to "hi", "th" to "th",
            "vi" to "vi", "id" to "id", "ms" to "ms", "pl" to "pl",
            "nl" to "nl", "sv" to "sv", "uk" to "uk", "pt" to "pt",
            "ar" to "ar"
        )[languageCode] ?: "zh"

        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(localeTag)
        )

        viewModelScope.launch {
            _languageApplied.send(Unit)
        }
    }
}
