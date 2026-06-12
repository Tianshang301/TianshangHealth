package com.tianshang.health.core.common.constants

object AppConstants {

    // Database
    const val DATABASE_NAME = "tianshang_health.db"
    const val DATABASE_VERSION = 1

    // DataStore
    const val DATASTORE_NAME = "tianshang_health_prefs"

    // WorkManager
    const val WORK_MANAGER_TAG = "tianshang_health_work"

    // Notification
    const val NOTIFICATION_CHANNEL_PERIOD = "period_reminder"
    const val NOTIFICATION_CHANNEL_OVULATION = "ovulation_reminder"
    const val NOTIFICATION_CHANNEL_PMS = "pms_reminder"
    const val NOTIFICATION_CHANNEL_CUSTOM = "custom_reminder"

    // Default values
    const val DEFAULT_CYCLE_LENGTH = 28
    const val DEFAULT_PERIOD_LENGTH = 5
    const val DEFAULT_LUTEAL_PHASE_LENGTH = 14
    const val MIN_CYCLES_FOR_PREDICTION = 3

    // Security
    const val BIOMETRIC_MAX_ATTEMPTS = 5
    const val BIOMETRIC_LOCKOUT_DURATION_MS = 30000L // 30 seconds

    // Backup
    const val BACKUP_FILE_EXTENSION = ".zip"
    const val BACKUP_FILE_PREFIX = "tianshang_health_backup_"

    // Date format
    const val DATE_FORMAT_DISPLAY = "yyyy-MM-dd"
    const val DATE_FORMAT_MONTH = "yyyy-MM"
    const val DATE_FORMAT_FULL = "yyyy-MM-dd HH:mm:ss"
}
