package com.tianshang.health.feature.steps.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tianshang.health.feature.steps.service.StepCounterService
import com.tianshang.health.feature.steps.service.StepSyncWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            StepCounterService.startService(context)
            StepSyncWorker.schedule(context)
        }
    }
}
