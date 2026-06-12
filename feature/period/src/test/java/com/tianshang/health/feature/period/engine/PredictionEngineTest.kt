package com.tianshang.health.feature.period.engine

import com.tianshang.health.core.database.entity.DailySymptom
import com.tianshang.health.core.database.entity.PeriodRecord
import com.tianshang.health.core.period.api.Confidence
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class PredictionEngineTest {

    private lateinit var engine: PredictionEngine

    @Before
    fun setUp() {
        engine = PredictionEngine()
    }

    @Test
    fun predict_returns_null_when_less_than_3_cycles() {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-01-01"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-01-29")
        )
        val result = engine.predict(records)
        assert(result == null) { "Prediction should be null with < 3 cycles" }
    }

    @Test
    fun predict_returns_result_when_3_or_more_cycles() {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-01-01", endDate = "2026-01-05"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-01-29", endDate = "2026-02-02"),
            PeriodRecord(id = 3, userId = 1, startDate = "2026-02-26", endDate = "2026-03-02")
        )
        val result = engine.predict(records)
        assert(result != null) { "Prediction should be non-null with >= 3 cycles" }
        assert(result!!.predictions.size == 6)
        // With 3 records, only 2 cycle lengths exist, so confidence = INSUFFICIENT_DATA
        assert(result.confidence == Confidence.INSUFFICIENT_DATA)
    }

    @Test
    fun predict_confidence_is_computed_with_4_or_more_cycles() {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-01-01", endDate = "2026-01-05"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-01-29", endDate = "2026-02-02"),
            PeriodRecord(id = 3, userId = 1, startDate = "2026-02-26", endDate = "2026-03-02"),
            PeriodRecord(id = 4, userId = 1, startDate = "2026-03-26", endDate = "2026-03-30")
        )
        val result = engine.predict(records)
        assert(result != null)
        // With 4 records, 3 cycle lengths -> confidence computed
        assert(result!!.confidence != Confidence.INSUFFICIENT_DATA)
    }

    @Test
    fun predict_uses_default_luteal_phase_when_no_symptoms() {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-01-01", endDate = "2026-01-05"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-01-29", endDate = "2026-02-02"),
            PeriodRecord(id = 3, userId = 1, startDate = "2026-02-26", endDate = "2026-03-02")
        )
        val result = engine.predict(records)

        val cycleLen = 28L
        val expectedOvulation = result!!.nextPeriodStart.minusDays(PredictionEngine.DEFAULT_LUTEAL_PHASE.toLong())
        assert(result.ovulationDate == expectedOvulation)
    }

    @Test
    fun predict_with_ovulation_test_positive_adjusts_ovulation() {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-01-01", endDate = "2026-01-05"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-01-29", endDate = "2026-02-02"),
            PeriodRecord(id = 3, userId = 1, startDate = "2026-02-26", endDate = "2026-03-02")
        )

        val symptoms = listOf(
            DailySymptom(userId = 1, date = "2026-03-09", ovulationTestResult = "positive")
        )

        val result = engine.predict(records, symptoms = symptoms)
        assert(result != null)
        val expectedOvulation = LocalDate.parse("2026-03-09")
        assert(result!!.ovulationDate == expectedOvulation) {
            "Ovulation should match positive test date, got ${result.ovulationDate}"
        }
    }

    @Test
    fun predict_with_BBT_temperature_rise_detects_ovulation() {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-01-01", endDate = "2026-01-05"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-01-29", endDate = "2026-02-02"),
            PeriodRecord(id = 3, userId = 1, startDate = "2026-02-26", endDate = "2026-03-02")
        )

        val symptoms = listOf(
            DailySymptom(userId = 1, date = "2026-03-08", bodyTemperature = 36.2f),
            DailySymptom(userId = 1, date = "2026-03-09", bodyTemperature = 36.5f),
            DailySymptom(userId = 1, date = "2026-03-10", bodyTemperature = 36.6f)
        )

        val result = engine.predict(records, symptoms = symptoms)
        assert(result != null)
        assert(result!!.ovulationDate == LocalDate.parse("2026-03-09")) {
            "Ovulation should be detected at temperature rise, got ${result.ovulationDate}"
        }
    }

    @Test
    fun predict_with_egg_white_mucus_detects_ovulation() {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-01-01", endDate = "2026-01-05"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-01-29", endDate = "2026-02-02"),
            PeriodRecord(id = 3, userId = 1, startDate = "2026-02-26", endDate = "2026-03-02")
        )

        val symptoms = listOf(
            DailySymptom(userId = 1, date = "2026-03-08", cervicalMucus = "egg_white")
        )

        val result = engine.predict(records, symptoms = symptoms)
        assert(result != null)
        assert(result!!.ovulationDate == LocalDate.parse("2026-03-08"))
    }

    @Test
    fun predict_handles_irregular_cycles_with_IQR_filtering() {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-01-01"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-01-30"),
            PeriodRecord(id = 3, userId = 1, startDate = "2026-03-01"),
            PeriodRecord(id = 4, userId = 1, startDate = "2026-03-28"),
            PeriodRecord(id = 5, userId = 1, startDate = "2026-04-26")
        )
        val result = engine.predict(records)
        assert(result != null)
        assert(result!!.cycleLength in 21..45)
    }

    @Test
    fun predict_confidence_is_HIGH_with_very_regular_cycles() {
        val records = (0 until 6).map { i ->
            PeriodRecord(
                id = i.toLong(),
                userId = 1,
                startDate = "2026-${(1 + i).toString().padStart(2, '0')}-01",
                endDate = "2026-${(1 + i).toString().padStart(2, '0')}-06"
            )
        }
        val result = engine.predict(records)
        assert(result != null)
        assert(result!!.confidence == Confidence.HIGH) {
            "Highly regular cycles should give HIGH confidence, got ${result.confidence}"
        }
    }

    @Test
    fun predict_confidence_is_LOW_with_irregular_cycles() {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-01-01"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-02-01"),
            PeriodRecord(id = 3, userId = 1, startDate = "2026-03-15"),
            PeriodRecord(id = 4, userId = 1, startDate = "2026-04-10"),
            PeriodRecord(id = 5, userId = 1, startDate = "2026-05-20")
        )
        val result = engine.predict(records)
        assert(result != null)
        assert(result!!.confidence == Confidence.LOW || result.confidence == Confidence.MEDIUM)
    }

    @Test
    fun calculateCycleRegularity_returns_Regular_for_low_CV() {
        val lengths = listOf(28, 28, 29, 28, 27)
        val result = engine.calculateCycleRegularity(lengths)
        assert(result == "Regular" || result == "Somewhat regular")
    }

    @Test
    fun calculateCycleRegularity_returns_Insufficient_data_for_under_3_cycles() {
        val lengths = listOf(28, 29)
        val result = engine.calculateCycleRegularity(lengths)
        assert(result == "Insufficient data")
    }

    @Test
    fun calculateCycleRegularity_handles_empty_list() {
        val result = engine.calculateCycleRegularity(emptyList())
        assert(result == "Insufficient data")
    }

    @Test
    fun predict_result_includes_6_future_predictions() {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-01-01"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-01-29"),
            PeriodRecord(id = 3, userId = 1, startDate = "2026-02-26")
        )
        val result = engine.predict(records)
        assert(result != null)
        assert(result!!.predictions.size == 6)
    }

    @Test
    fun predict_next_period_date_is_after_last_record() {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-01-01"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-01-29"),
            PeriodRecord(id = 3, userId = 1, startDate = "2026-02-26")
        )
        val result = engine.predict(records)
        assert(result != null)
        assert(result!!.nextPeriodStart.isAfter(LocalDate.parse("2026-02-26"))) {
            "Next period should be after last recorded start date"
        }
    }

    @Test
    fun calculateStatistics_returns_proper_stats_with_symptoms() {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-01-01", endDate = "2026-01-05"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-01-29", endDate = "2026-02-02"),
            PeriodRecord(id = 3, userId = 1, startDate = "2026-02-26", endDate = "2026-03-02")
        )
        val symptoms = listOf(
            DailySymptom(userId = 1, date = "2026-01-02", symptoms = "[\"头痛\",\"腹胀\"]"),
            DailySymptom(userId = 1, date = "2026-01-30", symptoms = "[\"头痛\"]")
        )
        val stats = engine.calculateStatistics(records, symptoms)
        assert(stats.totalCycles == 3)
        assert(stats.averageCycleLength > 0)
        assert(stats.averagePeriodLength > 0)
        assert(stats.symptomFrequency.containsKey("头痛"))
        assert(stats.symptomFrequency["头痛"] == 2)
        assert(stats.symptomFrequency["腹胀"] == 1)
    }

    @Test
    fun predict_handles_periods_without_endDate() {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-01-01"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-01-29"),
            PeriodRecord(id = 3, userId = 1, startDate = "2026-02-26")
        )
        val result = engine.predict(records)
        assert(result != null)
        assert(result!!.periodLength == PredictionEngine.DEFAULT_PERIOD_LENGTH)
    }

    @Test
    fun predict_with_custom_luteal_phase() {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-01-01"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-01-29"),
            PeriodRecord(id = 3, userId = 1, startDate = "2026-02-26")
        )
        val result = engine.predict(records, lutealPhaseLength = 12)
        assert(result != null)
        val expectedOvulation = result!!.nextPeriodStart.minusDays(12)
        assert(result.ovulationDate == expectedOvulation) {
            "Ovulation should use custom luteal phase of 12 days"
        }
    }
}
