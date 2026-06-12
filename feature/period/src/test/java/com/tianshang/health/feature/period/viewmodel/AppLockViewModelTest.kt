package com.tianshang.health.feature.period.viewmodel

import android.content.Context
import android.content.SharedPreferences
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.security.auth.AppLockManager
import com.tianshang.health.core.security.auth.Argon2Hasher
import com.tianshang.health.core.security.encryption.KeystoreManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
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
class AppLockViewModelTest {

    private val context: Context = mockk()
    private val stringResolver: StringResolver = mockk()
    private val prefs: SharedPreferences = mockk()
    private val prefsEditor: SharedPreferences.Editor = mockk()
    private lateinit var viewModel: AppLockViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(KeystoreManager)
        every { KeystoreManager.getEncryptedSharedPreferences(context) } returns prefs

        mockkObject(AppLockManager)
        every { AppLockManager.isPasswordSet() } returns false
        every { AppLockManager.unlock() } answers { Unit }
        every { AppLockManager.enable() } answers { Unit }
        every { AppLockManager.isEnabled } returns mockk {
            every { value } returns false
        }

        mockkObject(Argon2Hasher)
        coEvery { Argon2Hasher.hashPassword(any()) } returns (ByteArray(16) { 1 } to ByteArray(32) { 2 })

        every { prefs.edit() } returns prefsEditor
        every { prefsEditor.putString(any(), any()) } returns prefsEditor
        every { prefsEditor.apply() } returns Unit
        every { prefsEditor.putBoolean(any(), any()) } returns prefsEditor

        every { stringResolver.getString(R.string.pin_length_error, 4) } returns "PIN must be at least 4 digits"
        every { stringResolver.getString(R.string.pin_mismatch_error) } returns "PINs do not match"
        every { stringResolver.getString(R.string.error_failed_set_password) } returns "Failed to set password"
        every { stringResolver.getString(R.string.error_incorrect_password) } returns "Incorrect password"
        every { stringResolver.getString(R.string.error_verification_failed) } returns "Verification failed"
        every { stringResolver.getString(R.string.error_password_not_set) } returns "Password not set"

        viewModel = AppLockViewModel(context, stringResolver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initial_state_shows_create_mode_when_no_password() {
        val state = viewModel.state.value
        assert(!state.isPasswordSet)
        assert(state.mode == LockMode.CREATE)
    }

    @Test
    fun onDigit_appends_to_pin() {
        viewModel.onDigit('1')
        viewModel.onDigit('2')
        viewModel.onDigit('3')
        assert(viewModel.state.value.enteredPin == "123")
    }

    @Test
    fun onDigit_limits_to_6_digits() {
        for (d in 1..7) viewModel.onDigit(d.digitToChar())
        assert(viewModel.state.value.enteredPin.length == 6)
    }

    @Test
    fun onDelete_removes_last_digit() {
        viewModel.onDigit('1')
        viewModel.onDigit('2')
        viewModel.onDelete()
        assert(viewModel.state.value.enteredPin == "1")
    }

    @Test
    fun onClear_resets_pin_and_error() {
        viewModel.onDigit('1')
        viewModel.onClear()
        assert(viewModel.state.value.enteredPin == "")
    }

    @Test
    fun confirm_with_short_pin_shows_error() {
        viewModel.onDigit('1')
        viewModel.onDigit('2')
        viewModel.onDigit('3')
        viewModel.onConfirm()
        assert(viewModel.state.value.error != null)
    }

    @Test
    fun create_password_transitions_to_confirm() {
        viewModel.onDigit('1')
        viewModel.onDigit('2')
        viewModel.onDigit('3')
        viewModel.onDigit('4')
        viewModel.onConfirm()
        assert(viewModel.state.value.mode == LockMode.CONFIRM)
    }

    @Test
    fun confirm_password_calls_setPassword() = runTest {
        viewModel.onDigit('1')
        viewModel.onDigit('2')
        viewModel.onDigit('3')
        viewModel.onDigit('4')
        viewModel.onConfirm()

        viewModel.onDigit('1')
        viewModel.onDigit('2')
        viewModel.onDigit('3')
        viewModel.onDigit('4')
        viewModel.onConfirm()

        verify { Argon2Hasher.hashPassword("1234") }
    }

    @Test
    fun pin_mismatch_returns_to_create() {
        viewModel.onDigit('1')
        viewModel.onDigit('2')
        viewModel.onDigit('3')
        viewModel.onDigit('4')
        viewModel.onConfirm()

        viewModel.onDigit('5')
        viewModel.onDigit('6')
        viewModel.onDigit('7')
        viewModel.onDigit('8')
        viewModel.onConfirm()

        assert(viewModel.state.value.mode == LockMode.CREATE)
        assert(viewModel.state.value.enteredPin == "")
    }

    @Test
    fun onUnlocked_sets_authenticated() {
        viewModel.onUnlocked()
        assert(viewModel.state.value.isAuthenticated)
    }
}
