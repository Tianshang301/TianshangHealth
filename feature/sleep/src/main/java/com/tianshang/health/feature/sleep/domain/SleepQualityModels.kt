package com.tianshang.health.feature.sleep.domain

data class SleepQualityIndex(
    val overall: Int,
    val durationAdequacy: Int,
    val regularity: Int,
    val deepSleepRatio: Int,
    val continuity: Int
)

data class HealthInsight(
    val message: String,
    val type: InsightType,
    val dimension: String
)

enum class InsightType { POSITIVE, WARNING, CRITICAL, INFO }

data class SleepPhase(
    val startHour: Int,
    val endHour: Int,
    val phase: PhaseType
)

enum class PhaseType { LIGHT_SLEEP, DEEP_SLEEP, REM }
