package com.tianshang.health.feature.dashboard.domain

import androidx.compose.ui.graphics.vector.ImageVector

enum class InsightCategory { POSITIVE, WARNING, INFO }

data class InsightItem(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val category: InsightCategory
)

data class DashboardInsights(
    val items: List<InsightItem>,
    val hasData: Boolean
) {
    companion object {
        val EMPTY = DashboardInsights(emptyList(), false)
    }
}

data class PhaseStats(
    val follicularAvgSteps: Float? = null,
    val lutealAvgSteps: Float? = null,
    val follicularAvgMood: Float? = null,
    val lutealAvgMood: Float? = null,
    val follicularAvgSleep: Float? = null,
    val lutealAvgSleep: Float? = null,
    val follicularAvgStress: Float? = null,
    val lutealAvgStress: Float? = null,
    val follicularAvgCalories: Float? = null,
    val lutealAvgCalories: Float? = null,
    val periodMoodAvg: Float? = null,
    val periodSleepAvg: Float? = null,
    val periodStressAvg: Float? = null
)

sealed class InsightData {
    data class StepsActivity(val follicularAvg: Float, val lutealAvg: Float) : InsightData()
    data class MoodChange(val follicularAvg: Float, val lutealAvg: Float) : InsightData()
    data class SleepChange(val follicularAvg: Float, val lutealAvg: Float) : InsightData()
    data class StressChange(val periodAvg: Float, val follicularAvg: Float) : InsightData()
    data class CycleStepsResult(val follicularAvg: Float, val lutealAvg: Float) : InsightData()
    data class NutritionChange(val follicularAvgCalories: Float, val lutealAvgCalories: Float) : InsightData()
    data class TodayMood(val score: Int) : InsightData()
    data class TodayStress(val level: Int) : InsightData()
    data class TodaySleep(val quality: Int) : InsightData()
}
