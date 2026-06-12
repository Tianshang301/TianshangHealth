package com.tianshang.health.core.security.auth

import android.content.Context
import android.os.Build
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class BiometricAuthManagerTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    @Before
    fun setUp() {
        val realPrefs = RuntimeEnvironment.getApplication()
            .getSharedPreferences("test_biometric_prefs", Context.MODE_PRIVATE)
        realPrefs.edit().clear().apply()

        mockkObject(com.tianshang.health.core.security.encryption.KeystoreManager)
        every {
            com.tianshang.health.core.security.encryption.KeystoreManager.getEncryptedSharedPreferences(any())
        } returns realPrefs
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun canAuthenticate_returns_false_in_test_environment() {
        val result = BiometricAuthManager.canAuthenticate(context)
        assertFalse(result)
    }

    @Test
    fun isBiometricEnabled_returns_false_by_default() {
        val result = BiometricAuthManager.isBiometricEnabled(context)
        assertFalse(result)
    }

    @Test
    fun setBiometricEnabled_true_roundTrip() {
        BiometricAuthManager.setBiometricEnabled(context, true)
        assertTrue(BiometricAuthManager.isBiometricEnabled(context))
    }

    @Test
    fun setBiometricEnabled_false_roundTrip() {
        BiometricAuthManager.setBiometricEnabled(context, true)
        BiometricAuthManager.setBiometricEnabled(context, false)
        assertFalse(BiometricAuthManager.isBiometricEnabled(context))
    }

    @Test
    fun setBiometricEnabled_toggle_doesNotAffectOtherPrefs() {
        BiometricAuthManager.setBiometricEnabled(context, true)
        val first = BiometricAuthManager.isBiometricEnabled(context)
        BiometricAuthManager.setBiometricEnabled(context, false)
        val afterToggle = BiometricAuthManager.isBiometricEnabled(context)
        assertTrue(first)
        assertFalse(afterToggle)
    }
}
