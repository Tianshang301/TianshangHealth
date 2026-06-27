package com.tianshang.health.core.security.encryption

import android.content.Context
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom

object SqlCipherManager {

    private const val PREF_KEY_DB_PASSWORD = "k3x8m2w5"
    private const val PASSWORD_LENGTH = 32
    private const val CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"

    private val secureRandom = SecureRandom()

    fun getDatabasePassword(context: Context): String {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(context)

        var password = prefs.getString(PREF_KEY_DB_PASSWORD, null)
        if (password == null) {
            password = generateRandomPassword()
            prefs.edit().putString(PREF_KEY_DB_PASSWORD, password).apply()
        }

        return password
    }

    fun getSupportFactory(context: Context): SupportFactory {
        val password = getDatabasePassword(context)
        val bytes = password.toByteArray()
        try {
            return SupportFactory(bytes)
        } finally {
            bytes.fill(0)
        }
    }

    private fun generateRandomPassword(): String {
        val chars = CHARSET.toCharArray()
        val password = CharArray(PASSWORD_LENGTH)
        for (i in 0 until PASSWORD_LENGTH) {
            password[i] = chars[secureRandom.nextInt(chars.size)]
        }
        return String(password)
    }

    fun isDatabaseEncrypted(context: Context): Boolean {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
        return prefs.contains(PREF_KEY_DB_PASSWORD)
    }
}
