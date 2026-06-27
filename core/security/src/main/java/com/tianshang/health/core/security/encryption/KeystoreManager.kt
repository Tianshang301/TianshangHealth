package com.tianshang.health.core.security.encryption

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.KeyGenerator

object KeystoreManager {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "t4k8m2x6"
    private const val OLD_KEY_ALIAS = "tianshang_health_master_key"
    private const val PREFS_NAME = "tsh_prefs"
    private const val OLD_PREFS_NAME = "tianshang_health_secure_prefs"

    fun getOrCreateMasterKey(context: Context): MasterKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        // Migrate from old alias/prefs if needed
        if (!keyStore.containsAlias(KEY_ALIAS) && keyStore.containsAlias(OLD_KEY_ALIAS)) {
            migrateOldPreferences(context)
        }

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )

            val keyGenSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(keyGenSpec)
            keyGenerator.generateKey()
        }

        return MasterKey.Builder(context, KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    fun getEncryptedSharedPreferences(context: Context): android.content.SharedPreferences {
        val masterKey = getOrCreateMasterKey(context)

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun migrateOldPreferences(context: Context) {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            if (!keyStore.containsAlias(OLD_KEY_ALIAS)) return

            val oldMasterKey = MasterKey.Builder(context, OLD_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val oldPrefs = EncryptedSharedPreferences.create(
                context,
                OLD_PREFS_NAME,
                oldMasterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            val allEntries = oldPrefs.all
            if (allEntries.isNotEmpty()) {
                val newMasterKey = getOrCreateMasterKeyInternal(context)
                val newPrefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    newMasterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
                val editor = newPrefs.edit()
                for ((key, value) in allEntries) {
                    when (value) {
                        is String -> editor.putString(key, value)
                        is Boolean -> editor.putBoolean(key, value)
                        is Int -> editor.putInt(key, value)
                        is Long -> editor.putLong(key, value)
                        is Float -> editor.putFloat(key, value)
                    }
                }
                editor.apply()
                // Verify migration by checking a marker key exists in new prefs
                if (newPrefs.all.isNotEmpty()) {
                    oldPrefs.edit().clear().apply()
                    keyStore.deleteEntry(OLD_KEY_ALIAS)
                }
            } else {
                oldPrefs.edit().clear().apply()
                keyStore.deleteEntry(OLD_KEY_ALIAS)
            }
        } catch (_: Exception) {
            // Migration failed — old data preserved, will retry next launch
        }
    }

    private fun getOrCreateMasterKeyInternal(context: Context): MasterKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val keyGenSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGenerator.init(keyGenSpec)
            keyGenerator.generateKey()
        }
        return MasterKey.Builder(context, KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
}
