package com.tianshang.health.feature.sleep.domain

import com.tianshang.health.core.database.entity.DailyHealth
import org.junit.Test
import java.time.LocalDate

class SleepInsightEngineTest {

    private val engine = SleepInsightEngine()

    @Test
    fun less_than_7_records_returns_empty() {
        val records = (1..6).map {
            DailyHealth(userId = 1, date = LocalDate.now().minusDays(it.toLong()).toString(), sleepHours = 7f)
        }

        val insights = engine.generateInsights(records, null)

        assert(insights.isEmpty())
    }

    @Test
    fun improving_duration_generates_positive_insight() {
        val records = buildList {
            for (i in 14 downTo 8) {
                add(DailyHealth(userId = 1, date = LocalDate.now().minusDays(i.toLong()).toString(), sleepHours = 6f))
            }
            for (i in 7 downTo 1) {
                add(DailyHealth(userId = 1, date = LocalDate.now().minusDays(i.toLong()).toString(), sleepHours = 7.5f))
            }
        }

        val insights = engine.generateInsights(records, null)

        assert(insights.any { it.type == InsightType.POSITIVE && it.dimension == "duration" })
    }

    @Test
    fun declining_duration_generates_warning_insight() {
        val records = buildList {
            for (i in 14 downTo 8) {
                add(DailyHealth(userId = 1, date = LocalDate.now().minusDays(i.toLong()).toString(), sleepHours = 7.5f))
            }
            for (i in 7 downTo 1) {
                add(DailyHealth(userId = 1, date = LocalDate.now().minusDays(i.toLong()).toString(), sleepHours = 6f))
            }
        }

        val insights = engine.generateInsights(records, null)

        assert(insights.any { it.type == InsightType.WARNING && it.dimension == "duration" })
    }

    @Test
    fun high_quality_index_adds_positive_insight() {
        val records = (1..14).map {
            DailyHealth(userId = 1, date = LocalDate.now().minusDays(it.toLong()).toString(), sleepHours = 8f)
        }

        val insights = engine.generateInsights(records, SleepQualityIndex(85, 80, 80, 80, 80))

        assert(insights.any { it.type == InsightType.POSITIVE && it.dimension == "overall" })
    }

    @Test
    fun deep_sleep_deficit_detected() {
        val records = (1..14).map {
            DailyHealth(
                userId = 1,
                date = LocalDate.now().minusDays(it.toLong()).toString(),
                sleepHours = 8f,
                deepSleepHours = 0.8f
            )
        }

        val insights = engine.generateInsights(records, null)

        assert(insights.any { it.dimension == "deep" })
    }

    @Test
    fun empty_records_returns_empty() {
        val insights = engine.generateInsights(emptyList(), null)
        assert(insights.isEmpty())
    }
}
