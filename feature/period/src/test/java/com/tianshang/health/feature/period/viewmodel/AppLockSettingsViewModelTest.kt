package com.tianshang.health.feature.period.viewmodel

import android.content.Context
import com.tianshang.health.core.security.auth.AppLockManager
import com.tianshang.health.core.security.auth.BiometricAuthManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppLockSettingsViewModelTest {

    private val context: Context = mockk()

    private lateinit var viewModel: AppLockSettingsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(AppLockManager)
        mockkObject(BiometricAuthManager)

        every { AppLockManager.isEnabled } returns MutableStateFlow(false)
        every { AppLockManager.getLockDelayMs() } returns 0L
        every { AppLockManager.enable() } returns Unit
        every { AppLockManager.disable() } returns Unit
        every { AppLockManager.setLockDelayMs(any()) } returns Unit
        every { BiometricAuthManager.isBiometricEnabled(any()) } returns false
        every { BiometricAuthManager.setBiometricEnabled(any(), any()) } returns Unit

        viewModel = AppLockSettingsViewModel(context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initial_state_observes_lock_status() {
        assert(!viewModel.state.value.isEnabled)
    }

    @Test
    fun toggleLock_enables_and_disables() {
        viewModel.toggleLock(true)
        verify { AppLockManager.enable() }

        viewModel.toggleLock(false)
        verify { AppLockManager.disable() }
    }

    @Test
    fun toggleBiometric_saves_setting() {
        viewModel.toggleBiometric(true)
        verify { BiometricAuthManager.setBiometricEnabled(context, true) }
    }

    @Test
    fun setLockDelay_updates_delay() {
        viewModel.setLockDelay(30000L)
        verify { AppLockManager.setLockDelayMs(30000L) }
        assert(viewModel.state.value.lockDelayMs == 30000L)
    }

    @Test
    fun show_hide_change_pin_toggles_flag() {
        assert(!viewModel.state.value.showChangePinScreen)
        viewModel.showChangePin()
        assert(viewModel.state.value.showChangePinScreen)
        viewModel.hideChangePin()
        assert(!viewModel.state.value.showChangePinScreen)
    }
}
