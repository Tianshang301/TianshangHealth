package com.tianshang.health.feature.steps.service

import android.app.ActivityManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.feature.steps.data.local.StepCache
import com.tianshang.health.feature.steps.data.repository.StepsRepository
import com.tianshang.health.feature.steps.util.ActivityRecognitionPermissionHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class StepSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val stepsRepository: StepsRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "step_sync_work"

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<StepSyncWorker>(
                15,
                TimeUnit.MINUTES
            )
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            if (!ActivityRecognitionPermissionHelper.hasPermission(applicationContext)) {
                return Result.success()
            }

            // Health check: restart service if not running
            if (!isServiceRunning()) {
                StepCounterService.startService(applicationContext)
            }

            stepsRepository.initialize()

            // Read cached total steps and last synced sensor value
            val currentTotal = StepCache.getCachedTotalSteps(applicationContext)
            val lastSynced = StepCache.getLastSyncedSensorValue(applicationContext)

            if (currentTotal > lastSynced) {
                val missedSteps = currentTotal - lastSynced
                if (missedSteps > HealthConstants.MAX_STEPS_PER_INTERVAL) {
                    // Abnormal delta — skip adding but update markers to prevent retry
                    StepCache.setLastSyncedSensorValue(applicationContext, currentTotal)
                    StepCache.setLastRecordedTotal(applicationContext, currentTotal)
                } else if (missedSteps > 0) {
                    // Update cache markers FIRST to prevent Service from double-adding
                    StepCache.setLastSyncedSensorValue(applicationContext, currentTotal)
                    StepCache.setLastRecordedTotal(applicationContext, currentTotal)
                    // Then write to DB
                    stepsRepository.addSteps(missedSteps.toInt())
                }
            }

            Result.success()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun isServiceRunning(): Boolean {
        val manager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (StepCounterService::class.java.name == service.service.className) return true
        }
        return false
    }
}
