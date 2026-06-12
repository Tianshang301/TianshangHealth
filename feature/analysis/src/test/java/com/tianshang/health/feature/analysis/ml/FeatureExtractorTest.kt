package com.tianshang.health.feature.analysis.ml

import com.tianshang.health.core.database.entity.PeriodRecord
import org.junit.Before
import org.junit.Test

class FeatureExtractorTest {

    private lateinit var extractor: FeatureExtractor

    @Before
    fun setUp() {
        extractor = FeatureExtractor()
    }

    @Test
    fun `extractPeriodFeatures returns null with less than 2 records`() {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-01-01")
        )
        val result = extractor.extractPeriodFeatures(records, emptyList())
        assert(result == null) { "Expected null with < 2 records" }
    }

    @Test
    fun `extractPeriodFeatures computes cycle lengths correctly`() {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-01-01", endDate = "2026-01-05"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-01-29", endDate = "2026-02-02"),
            PeriodRecord(id = 3, userId = 1, startDate = "2026-02-26", endDate = "2026-03-02")
        )
        val result = extractor.extractPeriodFeatures(records, emptyList())
        assert(result != null)
        assert(result!!.cycleLengths.size == 2)
        assert(result.cycleLengths[0] == 28) { "Expected 28 days between Jan 1 and Jan 29" }
        assert(result.cycleLengths[1] == 28) { "Expected 28 days between Jan 29 and Feb 26" }
    }

    @Test
    fun `extractPeriodFeatures computes period lengths from endDate`() {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-01-01", endDate = "2026-01-06"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-01-29", endDate = "2026-02-02")
        )
        val result = extractor.extractPeriodFeatures(records, emptyList())
        assert(result != null)
        assert(result!!.periodLengths.size == 1)
        assert(result.periodLengths[0] == 5) { "Expected 5 days (Jan 29 to Feb 2 inclusive)" }
    }

    @Test
    fun `extractPeriodFeatures uses default period length when endDate null`() {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-01-01", endDate = "2026-01-05"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-01-29")
        )
        val result = extractor.extractPeriodFeatures(records, emptyList())
        assert(result != null)
        assert(result!!.periodLengths.size == 1)
        assert(result.periodLengths[0] == 5) { "Expected default 5 days when endDate is null" }
    }

    @Test
    fun `extractPeriodFeatures extracts pain levels`() {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-01-01", endDate = "2026-01-05", painLevel = 1),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-01-29", endDate = "2026-02-02", painLevel = 2)
        )
        val result = extractor.extractPeriodFeatures(records, emptyList())
        assert(result != null)
        assert(result!!.avgPainLevels.size == 1)
        assert(result.avgPainLevels[0] == 2f)
    }

    @Test
    fun `normalizeForLinear returns 6-element array`() {
        val features = FeatureExtractor.PeriodFeatures(
            cycleLengths = listOf(28, 29, 28),
            periodLengths = listOf(5, 4, 5),
            avgPainLevels = listOf(1f, 2f, 1f),
            lutealPhaseLength = 14,
            bbtSlope = 0.02f,
            symptomDiversityIndex = 3
        )
        val spec = ModelRegistry.getModelSpec(ModelRegistry.PERIOD_LINEAR)!!
        val input = extractor.normalizeForLinear(features, spec)
        assert(input.array.size == 6) { "Expected 6-element array for linear model" }
        assert(!input.isColdStart) { "3 cycles should not be cold start" }
    }

    @Test
    fun `normalizeForLstm returns full 72-element array with padding`() {
        val features = FeatureExtractor.PeriodFeatures(
            cycleLengths = (1..8).map { 28 },
            periodLengths = (1..8).map { 5 },
            avgPainLevels = (1..8).map { 1f },
            lutealPhaseLength = 14,
            bbtSlope = 0.01f,
            symptomDiversityIndex = 5
        )
        val spec = ModelRegistry.getModelSpec(ModelRegistry.PERIOD_LSTM)!!
        val input = extractor.normalizeForLstm(features, spec)
        assert(input.array.size == 12 * 6) { "Expected 12*6=72 elements (full LSTM input with padding)" }
        val expectedDataStart = (12 - 8) * 6
        assert(input.array[expectedDataStart] != 0f) { "First actual data element should be non-zero" }
        assert(input.array[expectedDataStart - 1] == 0f) { "Padding elements before data should be zero" }
    }

    @Test
    fun `normalizeForLstm caps at 12 cycles`() {
        val features = FeatureExtractor.PeriodFeatures(
            cycleLengths = (1..15).map { 28 },
            periodLengths = (1..15).map { 5 },
            avgPainLevels = (1..15).map { 1f },
            lutealPhaseLength = 14,
            bbtSlope = 0.01f,
            symptomDiversityIndex = 5
        )
        val spec = ModelRegistry.getModelSpec(ModelRegistry.PERIOD_LSTM)!!
        val input = extractor.normalizeForLstm(features, spec)
        assert(input.array.size == 12 * 6) { "Expected 12*6=72 elements (capped LSTM window)" }
    }

    @Test
    fun `denormalizePrediction reverses normalization correctly`() {
        val original = 28f
        val mean = 29.3f
        val std = 3.9f
        val normalized = (original - mean) / std
        val denormalized = extractor.denormalizePrediction(normalized, mean, std)
        assert(kotlin.math.abs(denormalized - original) < 0.01f) {
            "Denormalized $denormalized should be close to original $original"
        }
    }

    @Test
    fun `isColdStart true with less than 3 cycles`() {
        val features = FeatureExtractor.PeriodFeatures(
            cycleLengths = listOf(28, 29),
            periodLengths = listOf(5, 4),
            avgPainLevels = listOf(1f, 2f),
            lutealPhaseLength = 14,
            bbtSlope = 0f,
            symptomDiversityIndex = 1
        )
        val spec = ModelRegistry.getModelSpec(ModelRegistry.PERIOD_LINEAR)!!
        val input = extractor.normalizeForLinear(features, spec)
        assert(input.isColdStart) { "2 cycles should be cold start" }
    }
}
