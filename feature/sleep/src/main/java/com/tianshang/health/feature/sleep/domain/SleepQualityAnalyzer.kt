package com.tianshang.health.feature.sleep.domain

import com.tianshang.health.core.database.entity.DailyHealth

class SleepQualityAnalyzer {

    fun computeQualityIndex(records: List<DailyHealth>): SleepQualityIndex? {
        if (records.size < 3) return null

        val sleepHours = records.mapNotNull { it.sleepHours }
        val avgHours = if (sleepHours.isNotEmpty()) sleepHours.average().toFloat() else return null

        val bedTimes = records.map { it.bedTime }
        val wakeTimes = records.map { it.wakeTime }
        val latencies = records.map { it.sleepLatency }
        val wakeCounts = records.map { it.wakeCount }

        val deepHours = records.mapNotNull { it.deepSleepHours }
        val hasDeepData = deepHours.isNotEmpty() && sleepHours.isNotEmpty()
        val avgDeepRatio = if (hasDeepData) {
            deepHours.zip(sleepHours).map { (d, s) -> if (s > 0f) d / s else 0f }.average().toFloat()
        } else {
            0f
        }

        val deepScore = if (hasDeepData) scoreDeepSleepRatio(avgDeepRatio) else null

        val durationScore = scoreDurationAdequacy(avgHours)
        val regularityScore = scoreRegularity(bedTimes, wakeTimes)
        val continuityScore = scoreContinuity(latencies, wakeCounts)

        val overall = if (deepScore != null) {
            (durationScore * 0.4f + regularityScore * 0.25f + deepScore * 0.2f + continuityScore * 0.15f).toInt()
        } else {
            // redistribute 20% deep sleep weight: 0.4/0.8, 0.25/0.8, 0.15/0.8
            (durationScore * 0.5f + regularityScore * 0.3125f + continuityScore * 0.1875f).toInt()
        }

        return SleepQualityIndex(
            overall = overall.coerceIn(0, 100),
            durationAdequacy = durationScore,
            regularity = regularityScore,
            deepSleepRatio = deepScore ?: 50,
            continuity = continuityScore
        )
    }

    private fun scoreDurationAdequacy(avgHours: Float): Int {
        return when {
            avgHours in 7f..9f -> 100
            avgHours in 6f..7f || avgHours in 9f..10f -> 70
            avgHours in 5f..6f || avgHours in 10f..12f -> 40
            else -> 20
        }
    }

    private fun scoreRegularity(bedTimes: List<String?>, wakeTimes: List<String?>): Int {
        val bedRecords = bedTimes.filterNotNull()
        val wakeRecords = wakeTimes.filterNotNull()
        if (bedRecords.size < 3 && wakeRecords.size < 3) return 50

        fun minutes(time: String): Float? {
            val parts = time.split(":")
            if (parts.size != 2) return null
            return parts[0].toFloatOrNull()?.let { h ->
                parts[1].toFloatOrNull()?.let { m -> h * 60 + m }
            }
        }

        fun cv(values: List<Float>): Float? {
            if (values.size < 3) return null
            val mean = values.average().toFloat()
            if (mean == 0f) return null
            val variance = values.map { (it - mean) * (it - mean) }.average().toFloat()
            return kotlin.math.sqrt(variance) / mean
        }

        val bedCV = cv(bedRecords.mapNotNull { minutes(it) })
        val wakeCV = cv(wakeRecords.mapNotNull { minutes(it) })
        val bedScore = bedCV?.let { (1f - it.coerceIn(0f, 1f)) * 100f } ?: 50f
        val wakeScore = wakeCV?.let { (1f - it.coerceIn(0f, 1f)) * 100f } ?: 50f
        return ((bedScore + wakeScore) / 2f).toInt().coerceIn(0, 100)
    }

    private fun scoreDeepSleepRatio(avgRatio: Float): Int {
        return when {
            avgRatio >= 0.25f -> 100
            avgRatio >= 0.15f -> 60
            avgRatio >= 0.10f -> 30
            else -> 15
        }
    }

    private fun scoreContinuity(latencies: List<Int?>, wakeCounts: List<Int?>): Int {
        val avgLatency = latencies.filterNotNull().let { if (it.isNotEmpty()) it.average().toFloat() else null }
        val avgWakes = wakeCounts.filterNotNull().let { if (it.isNotEmpty()) it.average().toFloat() else null }

        val latencyScore = when {
            avgLatency == null -> 50
            avgLatency <= 15f -> 100
            avgLatency <= 30f -> 70
            avgLatency <= 60f -> 40
            else -> 20
        }
        val wakesScore = when {
            avgWakes == null -> 50
            avgWakes <= 1f -> 100
            avgWakes <= 2f -> 70
            avgWakes <= 3f -> 40
            else -> 20
        }
        return ((latencyScore + wakesScore) / 2f).toInt().coerceIn(0, 100)
    }
}
