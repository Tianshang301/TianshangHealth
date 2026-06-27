package com.tianshang.health.feature.steps.service

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.feature.steps.data.local.StepCache
import com.tianshang.health.feature.steps.data.repository.StepsRepository
import com.tianshang.health.feature.steps.util.ActivityRecognitionPermissionHelper
import com.tianshang.health.feature.steps.util.OemType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class StepCounterService : LifecycleService(), SensorEventListener {

    companion object {
        const val STEP_NOTIFICATION_ID = 1001
        const val ACTION_START = "com.tianshang.health.START_STEP_COUNTER"
        const val ACTION_STOP = "com.tianshang.health.STOP_STEP_COUNTER"
        private const val HEARTBEAT_INTERVAL_MS = 1000L
        private const val WAKE_LOCK_TIMEOUT_MS = 10 * 60 * 1000L
        private const val NANOS_TO_MILLIS = 1_000_000L

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
    }

    @Inject
    lateinit var stepsRepository: StepsRepository

    private lateinit var sensorManager: SensorManager
    private var stepCounter: Sensor? = null
    private var lastStepCount = -1L
    private var lastRecordedDate = ""
    private var isRunning = false
    private var wakeLock: PowerManager.WakeLock? = null

    private val stepDetector = StepDetector()
    private var accelerometerListener: SensorEventListener? = null

    @Volatile
    private var pendingAccelSteps = 0
    private var accelStepJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        // Start foreground ASAP — Android 14+ requires startForeground() within
        // a very short window after startForegroundService(), before any I/O.
        startForeground(
            STEP_NOTIFICATION_ID,
            StepNotificationHelper.createNotification(this, 0)
        )
        Log.d(TAG, "onCreate: startForeground called")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (!ActivityRecognitionPermissionHelper.hasPermission(this)) {
            Log.w(TAG, "Activity recognition permission not granted")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        when (intent?.action) {
            ACTION_START -> {
                if (!isRunning) {
                    isRunning = true

                    // startForeground() already called in onCreate()
                    lifecycleScope.launch {
                        try {
                            acquireWakeLock()
                            stepsRepository.initialize()
                            restoreBaseline()
                            startStepCounting()
                            StepNotificationHelper.updateNotification(
                                this@StepCounterService,
                                stepsRepository.getCurrentTodaySteps()
                            )
                        } catch (e: kotlinx.coroutines.CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to start service", e)
                            stopForeground(STOP_FOREGROUND_REMOVE)
                            stopSelf()
                        }
                    }
                } else {
                    StepNotificationHelper.updateNotification(
                        this@StepCounterService,
                        stepsRepository.getCurrentTodaySteps()
                    )
                }
            }
            ACTION_STOP -> {
                stopAndCleanUp()
            }
        }

        return START_STICKY
    }

    private suspend fun restoreBaseline() {
        val cachedBaseline = StepCache.getLastSensorBaseline(this)
        val cachedTotal = StepCache.getCachedTotalSteps(this)

        lastStepCount = when {
            cachedBaseline > 0 -> cachedBaseline
            cachedTotal > 0 -> cachedTotal
            else -> -1L
        }
        lastRecordedDate = LocalDate.now().toString()
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
                acquire(WAKE_LOCK_TIMEOUT_MS)
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Failed to acquire wake lock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Failed to release wake lock", e)
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
        accelerometerListener?.let { sensorManager.unregisterListener(it) }
        if (lastStepCount > 0) {
            StepCache.setLastSensorBaseline(this, lastStepCount)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val totalSteps = event.values[0].toLong()
        val currentDate = LocalDate.now().toString()

        when {
            lastStepCount < 0 -> {
                lastStepCount = totalSteps
                lastRecordedDate = currentDate
                StepCache.setLastSensorBaseline(this, totalSteps)
                StepCache.setCachedTotalSteps(this, totalSteps)
                StepCache.setLastRecordedTotal(this, totalSteps)
                StepCache.setLastSyncedSensorValue(this, totalSteps)
            }
            totalSteps < lastStepCount -> {
                // Reboot detected
                lastStepCount = totalSteps
                lastRecordedDate = currentDate
                StepCache.resetAfterReboot(this, totalSteps)
            }
            else -> {
                val newSteps = totalSteps - lastStepCount

                // Date boundary: if the date changed since last recording,
                // reset baseline to avoid attributing multi-day steps to today
                if (lastRecordedDate.isNotEmpty() && lastRecordedDate != currentDate) {
                    lastStepCount = totalSteps
                    lastRecordedDate = currentDate
                    StepCache.setLastSensorBaseline(this, totalSteps)
                    StepCache.setCachedTotalSteps(this, totalSteps)
                    StepCache.setLastRecordedTotal(this, totalSteps)
                    StepCache.setLastSyncedSensorValue(this, totalSteps)
                    Log.d(TAG, "Date boundary crossed ($lastRecordedDate → $currentDate), baseline reset")
                    return
                }

                if (newSteps > HealthConstants.MAX_STEPS_PER_INTERVAL) {
                    Log.w(TAG, "Stale baseline detected: delta=$newSteps, re-baselining")
                    lastStepCount = totalSteps
                    lastRecordedDate = currentDate
                    StepCache.resetAfterReboot(this, totalSteps)
                } else if (newSteps > 0) {
                    lastStepCount = totalSteps
                    lastRecordedDate = currentDate
                    StepCache.setLastSensorBaseline(this, totalSteps)
                    StepCache.setCachedTotalSteps(this, totalSteps)

                    lifecycleScope.launch {
                        try {
                            stepsRepository.addSteps(newSteps.toInt())
                            StepCache.setLastRecordedTotal(this@StepCounterService, totalSteps)
                            StepCache.setLastSyncedSensorValue(this@StepCounterService, totalSteps)
                            StepNotificationHelper.updateNotification(
                                this@StepCounterService,
                                stepsRepository.getCurrentTodaySteps()
                            )
                        } catch (e: kotlinx.coroutines.CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to add steps from sensor", e)
                        }
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun startAccelerometerFallback() {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer == null) {
            Log.w(TAG, "No accelerometer available")
            return
        }

        stepDetector.reset()
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                if (stepDetector.process(event.values, event.timestamp / NANOS_TO_MILLIS)) {
                    onStepDetected()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }
        accelerometerListener = listener

        sensorManager.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    private fun onStepDetected() {
        pendingAccelSteps++
        accelStepJob?.cancel()
        accelStepJob = lifecycleScope.launch {
            delay(HEARTBEAT_INTERVAL_MS)
            flushPendingSteps()
        }
    }

    private suspend fun flushPendingSteps() {
        val steps = pendingAccelSteps
        if (steps > 0) {
            pendingAccelSteps = 0
            try {
                stepsRepository.addSteps(steps)
                StepNotificationHelper.updateNotification(
                    this@StepCounterService,
                    stepsRepository.getCurrentTodaySteps()
                )
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add steps from accelerometer", e)
            }
        }
    }

    private fun stopAndCleanUp() {
        lifecycleScope.launch {
            flushPendingSteps()
            stopStepCounting()
            releaseWakeLock()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            isRunning = false
        }
    }

    override fun onDestroy() {
        lifecycleScope.launch {
            try {
                flushPendingSteps()
            } finally {
                stopStepCounting()
                releaseWakeLock()
                StepRestartWorker.schedule(this@StepCounterService)
            }
        }
        super.onDestroy()
    }
}

private const val TAG = "StepCounterService"
