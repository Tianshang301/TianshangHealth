package com.tianshang.health.core.security.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.tianshang.health.core.security.encryption.KeystoreManager

object BiometricAuthManager {

    private const val PREF_KEY_BIOMETRIC = "b8e2f5"

    fun canAuthenticate(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailed()
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun isBiometricEnabled(context: Context): Boolean {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
        return prefs.getBoolean(PREF_KEY_BIOMETRIC, false)
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
        prefs.edit().putBoolean(PREF_KEY_BIOMETRIC, enabled).apply()
    }

    fun authenticateForRecovery(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        showBiometricPrompt(
            activity = activity,
            title = activity.getString(com.tianshang.health.core.common.R.string.app_lock_recovery_title),
            subtitle = activity.getString(com.tianshang.health.core.common.R.string.app_lock_recovery_desc),
            onSuccess = onSuccess,
            onError = onError,
            onFailed = { onError(activity.getString(com.tianshang.health.core.common.R.string.biometric_init_failed)) }
        )
    }
}
