package com.tianshang.health.feature.steps.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object ActivityRecognitionPermissionHelper {

    fun requiredPermission(): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Manifest.permission.ACTIVITY_RECOGNITION
        } else {
            null
        }

    fun hasPermission(context: Context): Boolean {
        val permission = requiredPermission() ?: return true
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}
