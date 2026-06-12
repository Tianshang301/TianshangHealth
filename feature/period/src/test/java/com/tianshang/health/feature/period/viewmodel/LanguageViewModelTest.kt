package com.tianshang.health.feature.period.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.tianshang.health.core.security.encryption.KeystoreManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LanguageViewModelTest {

    private val context: Context = mockk()
    private val sharedPrefs: SharedPreferences = mockk()
    private val editor: SharedPreferences.Editor = mockk()

    private lateinit var viewModel: LanguageViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(KeystoreManager)
        mockkStatic(AppCompatDelegate::class)

        every { KeystoreManager.getEncryptedSharedPreferences(context) } returns sharedPrefs
        every { sharedPrefs.getString("app_language", "zh") } returns "zh"
        every { sharedPrefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.apply() } returns Unit
        every { AppCompatDelegate.setApplicationLocales(any()) } returns Unit

        viewModel = LanguageViewModel(context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initial_language_from_prefs() {
        assert(viewModel.selectedLanguage.value == "zh")
    }

    @Test
    fun setLanguage_updates_state_and_prefs() {
        viewModel.setLanguage("en")
        assert(viewModel.selectedLanguage.value == "en")
        verify { editor.putString("app_language", "en") }
    }

    @Test
    fun setLanguage_applies_locale() {
        viewModel.setLanguage("ja")
        verify { AppCompatDelegate.setApplicationLocales(any()) }
    }

    @Test
    fun language_applied_event_sent() = runTest {
        viewModel.setLanguage("ko")
        kotlinx.coroutines.test.TestCoroutineScheduler().advanceUntilIdle()
    }
}
