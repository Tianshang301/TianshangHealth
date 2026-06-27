package com.tianshang.health.feature.sleep.domain

import com.tianshang.health.core.database.entity.DailyHealth

class SleepInsightEngine {

    fun generateInsights(records: List<DailyHealth>, qualityIndex: SleepQualityIndex?): List<HealthInsight> {
        if (records.size < 7) return emptyList()

        val insights = mutableListOf<HealthInsight>()
        val recent = records.takeLast(7)
        val older = if (records.size >= 14) records.dropLast(7).takeLast(7) else emptyList()

        checkDurationTrend(recent, older)?.let { insights.add(it) }
        checkBedtimeDrift(records)?.let { insights.add(it) }
        checkSleepDebt(recent, optimalSleepHours(records))?.let { insights.add(it) }
        checkDeepSleepDeficit(recent)?.let { insights.add(it) }

        if (qualityIndex != null) {
            when {
                qualityIndex.overall >= 80 -> insights.add(
                    HealthInsight("Sleep quality is excellent. Keep it up!", InsightType.POSITIVE, "overall")
                )
                qualityIndex.overall < 50 -> insights.add(
                    HealthInsight(
                        "Sleep quality needs attention. Consider a consistent bedtime routine.",
                        InsightType.WARNING,
                        "overall"
                    )
                )
            }
        }

        return insights
    }

    private fun checkDurationTrend(recent: List<DailyHealth>, older: List<DailyHealth>): HealthInsight? {
        if (older.isEmpty()) return null
        val recentHours = recent.mapNotNull { it.sleepHours }
        val olderHours = older.mapNotNull { it.sleepHours }
        if (recentHours.isEmpty() || olderHours.isEmpty()) return null
        val recentAvg = recentHours.average()
        val olderAvg = olderHours.average()

        val diff = recentAvg - olderAvg
        return when {
            diff > 0.5f -> HealthInsight(
                "Sleep duration improved by %.1f hours".format(diff),
                InsightType.POSITIVE,
                "duration"
            )
            diff < -0.5f -> HealthInsight(
                "Sleep duration decreased by %.1f hours".format(-diff),
                InsightType.WARNING,
                "duration"
            )
            else -> null
        }
    }

    private fun checkBedtimeDrift(records: List<DailyHealth>): HealthInsight? {
        val bedTimes = records.mapNotNull { it.bedTime }
            .mapNotNull { time -> minutesSinceMidnight(time) }
            .map { if (it < 720f) it + 1440f else it } // normalize midnight crossing
        if (bedTimes.size < 7) return null

        val recent = bedTimes.takeLast(7).average()
        val older = bedTimes.take(7).average()
        val drift = recent - older
        return if (drift > 30f) {
            HealthInsight(
                "Bedtime is drifting later by about ${(drift / 60f).toInt()} hours",
                InsightType.WARNING,
                "regularity"
            )
        } else if (drift < -30f) {
            HealthInsight("Bedtime is shifting earlier. Good trend!", InsightType.POSITIVE, "regularity")
        } else {
            null
        }
    }

    private fun checkSleepDebt(recent: List<DailyHealth>, optimalHours: Float): HealthInsight? {
        val hours = recent.mapNotNull { it.sleepHours }
        if (hours.isEmpty()) return null
        val debt = hours.sumOf { (optimalHours - it).toDouble() }
        if (debt <= 0) return null
        return if (debt > 3.0) {
            HealthInsight("Sleep debt accumulated: %.1f hours".format(debt), InsightType.CRITICAL, "duration")
        } else if (debt > 1.0) {
            HealthInsight("Mild sleep debt: %.1f hours".format(debt), InsightType.WARNING, "duration")
        } else {
            null
        }
    }

    private fun checkDeepSleepDeficit(recent: List<DailyHealth>): HealthInsight? {
        val ratios = recent.mapNotNull { r ->
            val s = r.sleepHours ?: return@mapNotNull null
            val d = r.deepSleepHours ?: return@mapNotNull null
            if (s > 0f) d / s else null
        }
        if (ratios.isEmpty()) return null
        val avg = ratios.average()
        return when {
            avg >= 0.25 -> HealthInsight(
                "Deep sleep ratio %.0f%% is healthy".format(avg * 100),
                InsightType.POSITIVE,
                "deep"
            )
            avg < 0.15 -> HealthInsight(
                "Deep sleep ratio %.0f%% is below target (15%%)".format(avg * 100),
                InsightType.WARNING,
                "deep"
            )
            else -> null
        }
    }

    private fun optimalSleepHours(records: List<DailyHealth>): Float {
        val hours = records.mapNotNull { it.sleepHours }
        if (hours.isEmpty()) return 7.5f
        val sorted = hours.sorted()
        return sorted[sorted.size / 2]
    }

    private fun minutesSinceMidnight(time: String): Float? {
        val parts = time.split(":")
        if (parts.size != 2) return null
        return parts[0].toFloatOrNull()?.let { h ->
            parts[1].toFloatOrNull()?.let { m -> h * 60 + m }
        }
    }
}
