package com.tianshang.health.core.security.encryption

import android.content.Context
import android.os.Build
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class SqlCipherManagerTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    @Before
    fun setUp() {
        val realPrefs = RuntimeEnvironment.getApplication()
            .getSharedPreferences("test_secure_prefs", Context.MODE_PRIVATE)
        realPrefs.edit().clear().apply()

        mockkObject(KeystoreManager)
        every { KeystoreManager.getEncryptedSharedPreferences(any()) } returns realPrefs
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun getDatabasePassword_returns_nonEmptyString() {
        val password = SqlCipherManager.getDatabasePassword(context)
        assertNotNull(password)
        assertTrue(password.isNotEmpty())
    }

    @Test
    fun getDatabasePassword_has_length_32() {
        val password = SqlCipherManager.getDatabasePassword(context)
        assertTrue(password.length == 32)
    }

    @Test
    fun getDatabasePassword_returns_same_on_second_call() {
        val password1 = SqlCipherManager.getDatabasePassword(context)
        val password2 = SqlCipherManager.getDatabasePassword(context)
        assertTrue(password1 == password2)
    }

    @Test
    fun getDatabasePassword_contains_valid_chars() {
        val password = SqlCipherManager.getDatabasePassword(context)
        val validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        for (ch in password) {
            assertTrue("Illegal char $ch in password", ch in validChars)
        }
    }

    @Test
    fun getDatabasePassword_has_at_least_one_special_char() {
        val password = SqlCipherManager.getDatabasePassword(context)
        val specials = "!@#$%^&*"
        assertTrue(password.any { it in specials })
    }

    @Test
    fun getDatabasePassword_has_at_least_one_digit() {
        val password = SqlCipherManager.getDatabasePassword(context)
        assertTrue(password.any { it.isDigit() })
    }

    @Test
    fun getDatabasePassword_has_at_least_one_letter() {
        val password = SqlCipherManager.getDatabasePassword(context)
        assertTrue(password.any { it.isLetter() })
    }

    @Test
    fun isDatabaseEncrypted_stateTransitions() {
        assertFalse(SqlCipherManager.isDatabaseEncrypted(context))
        SqlCipherManager.getDatabasePassword(context)
        assertTrue(SqlCipherManager.isDatabaseEncrypted(context))
    }

    @Test
    fun getSupportFactory_returns_nonNull() {
        val factory = SqlCipherManager.getSupportFactory(context)
        assertNotNull(factory)
    }
}
