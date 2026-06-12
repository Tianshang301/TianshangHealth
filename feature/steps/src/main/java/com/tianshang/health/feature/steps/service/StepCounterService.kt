package com.tianshang.health.feature.steps.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.feature.steps.data.local.StepCache
import com.tianshang.health.feature.steps.data.repository.StepsRepository
import com.tianshang.health.feature.steps.util.OemType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StepCounterService : LifecycleService(), SensorEventListener {

    companion object {
        const val STEP_NOTIFICATION_ID = 1001
        const val STEP_CHANNEL_ID = "step_counter_channel"
        const val ACTION_START = "com.tianshang.health.START_STEP_COUNTER"
        const val ACTION_STOP = "com.tianshang.health.STOP_STEP_COUNTER"

        fun startService(context: Context) {
            val intent = Intent(context, StepCounterService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, StepCounterService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        private fun scheduleRestart(context: Context) {
            try {
                val intent = Intent(context, StepCounterService::class.java).apply {
                    action = ACTION_START
                }
                val pendingIntent = PendingIntent.getService(
                    context,
                    1002,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 5000,
                    pendingIntent
                )
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                Log.w("StepCounterService", "Failed to schedule alarm", e)
            }
        }
    }

    @Inject
    lateinit var stepsRepository: StepsRepository

    private lateinit var sensorManager: SensorManager
    private var stepCounter: Sensor? = null
    private var lastStepCount = -1L
    private var isRunning = false
    private var wakeLock: PowerManager.WakeLock? = null

    @Volatile
    private var pendingAccelSteps = 0
    private var accelStepJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // Restore last known sensor baseline across service restarts
        lastStepCount = StepCache.getLastSensorBaseline(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START -> {
                if (!isRunning) {
                    acquireWakeLock()
                    isRunning = true
                    lifecycleScope.launch {
                        stepsRepository.initialize()
                        startForeground(STEP_NOTIFICATION_ID, buildNotification(stepsRepository.getCurrentTodaySteps()))
                        startStepCounting()
                    }
                } else {
                    // Already running, update notification with current count
                    updateNotification(stepsRepository.getCurrentTodaySteps())
                }
            }
            ACTION_STOP -> {
                stopStepCounting()
                releaseWakeLock()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                isRunning = false
            }
        }

        return START_STICKY
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            val oemType = OemType.detect()
            val tag = when (oemType) {
                OemType.HUAWEI, OemType.HONOR -> "LocationManagerService"
                else -> "TianshangHealth:StepCounterWakeLock"
            }
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                tag
            ).apply {
                acquire()
            }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
            Log.w("StepCounterService", "Failed to acquire wake lock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
            Log.w("StepCounterService", "Failed to release wake lock", e)
        }
    }

    private fun startStepCounting() {
        if (stepCounter == null) {
            startAccelerometerFallback()
            return
        }

        sensorManager.registerListener(
            this,
            stepCounter,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    private fun stopStepCounting() {
        sensorManager.unregisterListener(this)
        sensorManager.unregisterListener(accelerometerListener)
        // Persist current baseline so we don't lose it on service restart
        if (lastStepCount > 0) {
            StepCache.setLastSensorBaseline(this, lastStepCount)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val totalSteps = event.values[0].toLong()

        if (lastStepCount == -1L) {
            lastStepCount = totalSteps
            StepCache.setLastSensorBaseline(this, totalSteps)
            return
        }

        if (totalSteps < lastStepCount) {
            // Sensor reset (e.g. device reboot) — re-baseline
            lastStepCount = totalSteps
            StepCache.setLastSensorBaseline(this, totalSteps)
            return
        }

        val newSteps = totalSteps - lastStepCount
        if (newSteps > 0) {
            lastStepCount = totalSteps
            StepCache.setLastSensorBaseline(this, totalSteps)
            StepCache.setCachedTotalSteps(this, totalSteps)

            lifecycleScope.launch {
                try {
                    stepsRepository.addSteps(newSteps.toInt())
                    updateNotification(stepsRepository.getCurrentTodaySteps())
                } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                    Log.e("StepCounterService", "Failed to add steps from sensor", e)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun startAccelerometerFallback() {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer != null) {
            sensorManager.registerListener(
                accelerometerListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    private val accelerometerListener = object : SensorEventListener {
        private val window = ArrayDeque<Float>(20)
        private var lastStepTime = 0L
        private var calibrated = false
        private var threshold = 1.5f

        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return

            val ax = event.values[0]
            val ay = event.values[1]
            val az = event.values[2]
            val magnitude = Math.sqrt(
                (ax * ax + ay * ay + az * az).toDouble()
            ).toFloat()

            if (!calibrated) {
                window.addLast(magnitude)
                if (window.size >= 20) {
                    val avg = window.average().toFloat()
                    threshold = avg * 0.15f
                    if (threshold < 0.8f) threshold = 0.8f
                    calibrated = true
                }
                return
            }

            val deviation = Math.abs(magnitude - (window.average().toFloat()))
            if (deviation > threshold && magnitude in 9.0f..15.0f) {
                val now = System.currentTimeMillis()
                if (now - lastStepTime > HealthConstants.STEP_DEBOUNCE_MS) {
                    lastStepTime = now
                    onStepDetected()
                }
            }

            window.addLast(magnitude)
            if (window.size > 20) window.removeFirst()
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

    private fun onStepDetected() {
        pendingAccelSteps++
        accelStepJob?.cancel()
        accelStepJob = lifecycleScope.launch {
            delay(1000)
            val steps = pendingAccelSteps
            if (steps > 0) {
                pendingAccelSteps = 0
                try {
                    stepsRepository.addSteps(steps)
                    updateNotification(stepsRepository.getCurrentTodaySteps())
                } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                    Log.e("StepCounterService", "Failed to add steps from accelerometer", e)
                }
            }
        }
    }

    private fun buildNotification(steps: Int): Notification {
        createNotificationChannel()

        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, STEP_CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.notification_steps_body, steps))
            .setContentText(applicationContext.getString(R.string.step_notification_text))
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification(steps: Int) {
        val notification = buildNotification(steps)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(STEP_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                STEP_CHANNEL_ID,
                applicationContext.getString(R.string.notification_channel_steps),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = applicationContext.getString(R.string.notification_channel_steps_desc)
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopStepCounting()
        releaseWakeLock()
        scheduleRestart(this)
    }
}
