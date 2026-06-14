package com.tianshang.health.feature.steps.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.tianshang.health.core.common.R

internal object StepNotificationHelper {

    private const val STEP_NOTIFICATION_ID = 1001
    private const val STEP_CHANNEL_ID = "step_counter_channel"

    fun createNotification(context: Context, steps: Int): Notification {
        createNotificationChannel(context)

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, STEP_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notification_steps_body, steps))
            .setContentText(context.getString(R.string.step_notification_text))
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun updateNotification(context: Context, steps: Int) {
        val notification = createNotification(context, steps)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(STEP_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                STEP_CHANNEL_ID,
                context.getString(R.string.notification_channel_steps),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_steps_desc)
                setShowBadge(false)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
