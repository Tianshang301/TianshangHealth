package com.tianshang.health.feature.sleep.domain

import com.tianshang.health.core.database.entity.DailyHealth
import org.junit.Test

class SleepQualityAnalyzerTest {

    private val analyzer = SleepQualityAnalyzer()

    @Test
    fun ideal_data_returns_high_score() {
        val records = listOf(
            DailyHealth(
                userId = 1, date = "2026-06-01", sleepHours = 8f, deepSleepHours = 2.2f, sleepQuality = 5,
                bedTime = "23:00", wakeTime = "07:00", sleepLatency = 10, wakeCount = 0
            ),
            DailyHealth(
                userId = 1, date = "2026-06-02", sleepHours = 7.8f, deepSleepHours = 2.1f, sleepQuality = 5,
                bedTime = "23:00", wakeTime = "06:50", sleepLatency = 12, wakeCount = 1
            ),
            DailyHealth(
                userId = 1, date = "2026-06-03", sleepHours = 8.2f, deepSleepHours = 2.3f, sleepQuality = 4,
                bedTime = "23:00", wakeTime = "07:10", sleepLatency = 8, wakeCount = 0
            ),
            DailyHealth(
                userId = 1, date = "2026-06-04", sleepHours = 7.9f, deepSleepHours = 2.0f, sleepQuality = 5,
                bedTime = "23:00", wakeTime = "07:00", sleepLatency = 11, wakeCount = 1
            ),
            DailyHealth(
                userId = 1, date = "2026-06-05", sleepHours = 8f, deepSleepHours = 2.2f, sleepQuality = 4,
                bedTime = "23:00", wakeTime = "07:00", sleepLatency = 10, wakeCount = 0
            )
        )

        val result = analyzer.computeQualityIndex(records)

        assert(result != null)
        assert(result!!.overall >= 80) { "Expected >=80, got ${result.overall}" }
    }

    @Test
    fun poor_data_returns_low_score() {
        val records = listOf(
            DailyHealth(
                userId = 1, date = "2026-06-01", sleepHours = 4f, deepSleepHours = 0.3f, sleepQuality = 1,
                bedTime = "23:00", wakeTime = "03:00", sleepLatency = 60, wakeCount = 4
            ),
            DailyHealth(
                userId = 1, date = "2026-06-02", sleepHours = 5f, deepSleepHours = 0.5f, sleepQuality = 2,
                bedTime = "01:00", wakeTime = "06:00", sleepLatency = 45, wakeCount = 3
            ),
            DailyHealth(
                userId = 1, date = "2026-06-03", sleepHours = 4.5f, deepSleepHours = 0.4f, sleepQuality = 1,
                bedTime = "02:00", wakeTime = "06:30", sleepLatency = 55, wakeCount = 4
            )
        )

        val result = analyzer.computeQualityIndex(records)

        assert(result != null)
        assert(result!!.overall < 50) { "Expected <50, got ${result.overall}" }
    }

    @Test
    fun less_than_3_records_returns_null() {
        val records = listOf(
            DailyHealth(userId = 1, date = "2026-06-01", sleepHours = 7f),
            DailyHealth(userId = 1, date = "2026-06-02", sleepHours = 8f)
        )

        val result = analyzer.computeQualityIndex(records)

        assert(result == null)
    }

    @Test
    fun null_sleepHours_returns_null() {
        val records = listOf(
            DailyHealth(userId = 1, date = "2026-06-01"),
            DailyHealth(userId = 1, date = "2026-06-02"),
            DailyHealth(userId = 1, date = "2026-06-03")
        )

        val result = analyzer.computeQualityIndex(records)

        assert(result == null)
    }

    @Test
    fun duration_score_7_to_9_is_100() {
        val analyzer = SleepQualityAnalyzer()
        val records = listOf(
            DailyHealth(userId = 1, date = "2026-06-01", sleepHours = 8f, deepSleepHours = 2f),
            DailyHealth(userId = 1, date = "2026-06-02", sleepHours = 8f, deepSleepHours = 2f),
            DailyHealth(userId = 1, date = "2026-06-03", sleepHours = 8f, deepSleepHours = 2f)
        )

        val result = analyzer.computeQualityIndex(records)

        assert(result != null)
        assert(result!!.durationAdequacy == 100)
    }

    @Test
    fun empty_records_returns_null() {
        val result = analyzer.computeQualityIndex(emptyList())
        assert(result == null)
    }

    @Test
    fun deep_ratio_25pct_is_full_score() {
        val analyzer = SleepQualityAnalyzer()
        val records = listOf(
            DailyHealth(userId = 1, date = "2026-06-01", sleepHours = 8f, deepSleepHours = 2.5f),
            DailyHealth(userId = 1, date = "2026-06-02", sleepHours = 8f, deepSleepHours = 2.4f),
            DailyHealth(userId = 1, date = "2026-06-03", sleepHours = 8f, deepSleepHours = 2.6f)
        )

        val result = analyzer.computeQualityIndex(records)

        assert(result != null)
        assert(result!!.deepSleepRatio == 100)
    }

    @Test
    fun edge_case_5h_duration_scores_40() {
        val records = listOf(
            DailyHealth(userId = 1, date = "2026-06-01", sleepHours = 5f, deepSleepHours = 0.5f),
            DailyHealth(userId = 1, date = "2026-06-02", sleepHours = 5.5f, deepSleepHours = 0.5f),
            DailyHealth(userId = 1, date = "2026-06-03", sleepHours = 5.2f, deepSleepHours = 0.5f)
        )

        val result = analyzer.computeQualityIndex(records) ?: return

        assert(result.durationAdequacy == 40)
    }

    @Test
    fun mixed_bedtime_data_scores_midrange() {
        val records = listOf(
            DailyHealth(userId = 1, date = "2026-06-01", sleepHours = 7f, bedTime = "23:00", wakeTime = "07:00"),
            DailyHealth(userId = 1, date = "2026-06-02", sleepHours = 7f, bedTime = "22:00", wakeTime = "07:00"),
            DailyHealth(userId = 1, date = "2026-06-03", sleepHours = 7f, bedTime = "00:00", wakeTime = "07:00")
        )

        val result = analyzer.computeQualityIndex(records) ?: return

        assert(result.regularity in 1..99)
    }
}
