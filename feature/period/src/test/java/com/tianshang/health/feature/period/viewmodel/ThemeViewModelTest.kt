package com.tianshang.health.feature.period.viewmodel

import android.content.Context
import android.content.SharedPreferences
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.security.encryption.KeystoreManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    private val context: Context = mockk()
    private val sharedPrefs: SharedPreferences = mockk()
    private val wallpaperPrefs: SharedPreferences = mockk()
    private val editor: SharedPreferences.Editor = mockk()
    private val stringResolver: StringResolver = mockk()

    private lateinit var viewModel: ThemeViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(KeystoreManager)
        every { KeystoreManager.getEncryptedSharedPreferences(context) } returns sharedPrefs
        every { sharedPrefs.getFloat("theme_hue", 340f) } returns 340f
        every { sharedPrefs.getFloat("theme_saturation", 0.6f) } returns 0.6f
        every { sharedPrefs.getFloat("theme_lightness", 0.7f) } returns 0.7f
        every { sharedPrefs.getBoolean("use_custom_theme", false) } returns false
        every { sharedPrefs.edit() } returns editor
        every { editor.putFloat(any(), any()) } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.apply() } returns Unit

        every { context.getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE) } returns wallpaperPrefs
        every { wallpaperPrefs.getBoolean(any(), any()) } returns false
        every { wallpaperPrefs.getInt(any(), any()) } returns 0
        every { wallpaperPrefs.getFloat(any(), any()) } returns 0f
        every { wallpaperPrefs.getString(any(), any()) } returns ""

        every { stringResolver.getString(any()) } returns "error"

        viewModel = ThemeViewModel(context, stringResolver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initial_values_from_prefs() {
        assert(viewModel.hue.value == 340f)
        assert(viewModel.saturation.value == 0.6f)
        assert(viewModel.lightness.value == 0.7f)
        assert(!viewModel.useCustomTheme.value)
    }

    @Test
    fun setHue_updates_state_and_prefs() {
        viewModel.setHue(180f)
        assert(viewModel.hue.value == 180f)
        verify { editor.putFloat("theme_hue", 180f) }
    }

    @Test
    fun applyPreset_sets_all_values() {
        viewModel.applyPreset(250f, 0.7f, 0.4f)
        assert(viewModel.hue.value == 250f)
        assert(viewModel.saturation.value == 0.7f)
        assert(viewModel.lightness.value == 0.4f)
    }

    @Test
    fun toggleCustomTheme_updates_state() {
        viewModel.toggleCustomTheme(true)
        assert(viewModel.useCustomTheme.value)
        verify { editor.putBoolean("use_custom_theme", true) }
    }
}
