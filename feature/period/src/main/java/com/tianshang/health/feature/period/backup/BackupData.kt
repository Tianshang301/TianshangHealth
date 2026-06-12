package com.tianshang.health.feature.period.backup

import com.tianshang.health.core.database.entity.DailyHealth
import com.tianshang.health.core.database.entity.DailySteps
import com.tianshang.health.core.database.entity.DailySymptom
import com.tianshang.health.core.database.entity.PeriodRecord
import com.tianshang.health.core.database.entity.User

data class BackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val users: List<User> = emptyList(),
    val periodRecords: List<PeriodRecord> = emptyList(),
    val dailySymptoms: List<DailySymptom> = emptyList(),
    val dailyHealth: List<DailyHealth> = emptyList(),
    val dailySteps: List<DailySteps> = emptyList()
)
