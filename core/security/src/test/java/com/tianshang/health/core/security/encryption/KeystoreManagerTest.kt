package com.tianshang.health.core.security.encryption

import android.content.Context
import android.os.Build
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
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
class KeystoreManagerTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    @Before
    fun setUp() {
        val realPrefs = RuntimeEnvironment.getApplication()
            .getSharedPreferences("test_keystore_prefs", Context.MODE_PRIVATE)
        realPrefs.edit().clear().apply()

        mockkObject(KeystoreManager)
        every { KeystoreManager.getEncryptedSharedPreferences(any()) } returns realPrefs
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun getEncryptedSharedPreferences_returns_nonNull() {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
        assertNotNull(prefs)
    }

    @Test
    fun getEncryptedSharedPreferences_roundTrip_persistsStringValue() {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
        prefs.edit().putString("test_key", "encrypted_value").apply()
        val result = prefs.getString("test_key", null)
        assertTrue(result == "encrypted_value")
    }

    @Test
    fun getEncryptedSharedPreferences_roundTrip_persistsBooleanValue() {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
        prefs.edit().putBoolean("flag", true).apply()
        assertTrue(prefs.getBoolean("flag", false))
    }

    @Test
    fun getEncryptedSharedPreferences_missingKey_returnsDefault() {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
        assertTrue(prefs.getString("nonexistent", "default") == "default")
    }

    @Test
    fun getEncryptedSharedPreferences_roundTrip_persistsIntValue() {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
        prefs.edit().putInt("counter", 42).apply()
        assertTrue(prefs.getInt("counter", 0) == 42)
    }

    @Test
    fun getEncryptedSharedPreferences_clear_removesKey() {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
        prefs.edit().putString("temp", "value").apply()
        prefs.edit().remove("temp").apply()
        assertTrue(prefs.getString("temp", null) == null)
    }
}
