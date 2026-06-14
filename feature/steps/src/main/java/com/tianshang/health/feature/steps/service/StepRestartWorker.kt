package com.tianshang.health.feature.steps.service

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * One-time worker that restarts the step counter service after it has been killed.
 * Used from onDestroy() as a more reliable fallback than AlarmManager on modern Android.
 */
@HiltWorker
class StepRestartWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val WORK_NAME = "step_restart_work"

        fun schedule(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<StepRestartWorker>()
                .setInitialDelay(1, TimeUnit.MINUTES)
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            StepCounterService.startService(applicationContext)
            Result.success()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("StepRestartWorker", "Failed to restart step service", e)
            Result.retry()
        }
    }
}
