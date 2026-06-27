package com.tianshang.health.feature.steps.data.local

import android.content.Context
import com.tianshang.health.core.security.encryption.KeystoreManager

object StepCache {

    private const val PREF_KEY_TOTAL_STEPS = "total_steps"
    private const val PREF_KEY_LAST_RECORDED_TOTAL = "last_recorded_total"
    private const val PREF_KEY_LAST_SENSOR_BASELINE = "last_sensor_baseline"
    private const val PREF_KEY_LAST_SYNCED_SENSOR_VALUE = "last_synced_sensor_value"

    fun getCachedTotalSteps(context: Context): Long {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
        return prefs.getLong(PREF_KEY_TOTAL_STEPS, 0L)
    }

    fun setCachedTotalSteps(context: Context, steps: Long) {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
        prefs.edit().putLong(PREF_KEY_TOTAL_STEPS, steps).apply()
    }

    fun getLastRecordedTotal(context: Context): Long {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
        return prefs.getLong(PREF_KEY_LAST_RECORDED_TOTAL, 0L)
    }

    fun setLastRecordedTotal(context: Context, steps: Long) {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
        prefs.edit().putLong(PREF_KEY_LAST_RECORDED_TOTAL, steps).apply()
    }

    fun getLastSensorBaseline(context: Context): Long {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
        return prefs.getLong(PREF_KEY_LAST_SENSOR_BASELINE, -1L)
    }

    fun setLastSensorBaseline(context: Context, steps: Long) {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
        prefs.edit().putLong(PREF_KEY_LAST_SENSOR_BASELINE, steps).apply()
    }

    fun getLastSyncedSensorValue(context: Context): Long {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
        return prefs.getLong(PREF_KEY_LAST_SYNCED_SENSOR_VALUE, 0L)
    }

    fun setLastSyncedSensorValue(context: Context, steps: Long) {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
        prefs.edit().putLong(PREF_KEY_LAST_SYNCED_SENSOR_VALUE, steps).apply()
    }

    fun resetAfterReboot(context: Context, newSensorValue: Long) {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
        prefs.edit()
            .putLong(PREF_KEY_LAST_SENSOR_BASELINE, newSensorValue)
            .putLong(PREF_KEY_TOTAL_STEPS, newSensorValue)
            .putLong(PREF_KEY_LAST_RECORDED_TOTAL, newSensorValue)
            .putLong(PREF_KEY_LAST_SYNCED_SENSOR_VALUE, newSensorValue)
            .apply()
    }
}
