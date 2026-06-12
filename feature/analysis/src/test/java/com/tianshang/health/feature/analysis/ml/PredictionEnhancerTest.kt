package com.tianshang.health.feature.analysis.ml

import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.database.entity.PeriodRecord
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class PredictionEnhancerTest {

    private lateinit var tFLiteManager: TFLiteManager
    private lateinit var featureExtractor: FeatureExtractor
    private lateinit var stringResolver: StringResolver
    private lateinit var enhancer: PredictionEnhancer

    @Before
    fun setUp() {
        tFLiteManager = mockk(relaxed = true)
        featureExtractor = FeatureExtractor()
        stringResolver = mockk()
        every { stringResolver.getString(any()) } answers { "[${it.invocation.args[0]}]" }
        every { stringResolver.getString(any(), *anyVararg()) } answers {
            val resId = it.invocation.args[0]
            val args = it.invocation.args.drop(1).joinToString(",")
            "[$resId:$args]"
        }
        enhancer = PredictionEnhancer(tFLiteManager, featureExtractor, stringResolver)
    }

    private fun makeRecords(
        count: Int,
        baseDate: String = "2026-01-01",
        cycleLen: Int = 28,
        periodLen: Int = 5
    ): List<PeriodRecord> {
        val records = mutableListOf<PeriodRecord>()
        var date = java.time.LocalDate.parse(baseDate)
        for (i in 1..count) {
            records.add(
                PeriodRecord(
                    id = i.toLong(),
                    userId = 1L,
                    startDate = date.toString(),
                    endDate = date.plusDays(periodLen.toLong() - 1).toString()
                )
            )
            date = date.plusDays(cycleLen.toLong())
        }
        return records
    }

    @Test
    fun `enhance returns fallback with less than 3 cycles`() {
        val records = makeRecords(2)
        val result = enhancer.enhance(records, emptyList())
        assert(result.isFallback)
        assert(result.modelUsed == null)
        assert(result.confidence == 0f)
    }

    @Test
    fun `enhance calls linear inference with 3 cycles`() {
        every { tFLiteManager.isModelAvailable(ModelRegistry.PERIOD_LINEAR) } returns true
        every { tFLiteManager.runInference(ModelRegistry.PERIOD_LINEAR, any(), any()) } returns
            floatArrayOf(0f, 0.9f)

        val records = makeRecords(4)
        val result = enhancer.enhance(records, emptyList())

        verify { tFLiteManager.runInference(ModelRegistry.PERIOD_LINEAR, any(), any()) }
        assert(!result.isFallback)
        assert(result.modelUsed == ModelRegistry.PERIOD_LINEAR)
    }

    @Test
    fun `enhance falls back when linear model not available`() {
        every { tFLiteManager.isModelAvailable(ModelRegistry.PERIOD_LINEAR) } returns false

        val records = makeRecords(4)
        val result = enhancer.enhance(records, emptyList())

        assert(result.isFallback)
    }

    @Test
    fun `enhance calls LSTM inference with 6+ cycles`() {
        every { tFLiteManager.isModelAvailable(ModelRegistry.PERIOD_LSTM) } returns true
        every { tFLiteManager.runInference(ModelRegistry.PERIOD_LSTM, any(), any()) } returns
            floatArrayOf(0f, 0.9f)

        val records = makeRecords(7)
        val result = enhancer.enhance(records, emptyList())

        verify { tFLiteManager.runInference(ModelRegistry.PERIOD_LSTM, any(), any()) }
        assert(!result.isFallback)
        assert(result.modelUsed == ModelRegistry.PERIOD_LSTM)
    }

    @Test
    fun `enhance cascades to linear when LSTM returns null`() {
        every { tFLiteManager.isModelAvailable(ModelRegistry.PERIOD_LSTM) } returns true
        every { tFLiteManager.runInference(ModelRegistry.PERIOD_LSTM, any(), any()) } returns null
        every { tFLiteManager.isModelAvailable(ModelRegistry.PERIOD_LINEAR) } returns true
        every { tFLiteManager.runInference(ModelRegistry.PERIOD_LINEAR, any(), any()) } returns
            floatArrayOf(0f, 0.85f)

        val records = makeRecords(7)
        val result = enhancer.enhance(records, emptyList())

        verify { tFLiteManager.runInference(ModelRegistry.PERIOD_LSTM, any(), any()) }
        verify { tFLiteManager.runInference(ModelRegistry.PERIOD_LINEAR, any(), any()) }
        assert(!result.isFallback)
        assert(result.modelUsed == ModelRegistry.PERIOD_LINEAR)
    }

    @Test
    fun `enhance falls back when confidence below threshold`() {
        every { tFLiteManager.isModelAvailable(ModelRegistry.PERIOD_LSTM) } returns true
        every { tFLiteManager.runInference(ModelRegistry.PERIOD_LSTM, any(), any()) } returns
            floatArrayOf(0f, 0.2f)

        // Single very short record - confidence 0 due to insufficient cycles
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-01-01", endDate = "2026-01-05"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-01-08", endDate = "2026-01-12"),
            PeriodRecord(id = 3, userId = 1, startDate = "2026-01-15", endDate = "2026-01-19"),
            PeriodRecord(id = 4, userId = 1, startDate = "2026-04-01", endDate = "2026-04-05")
        )
        val result = enhancer.enhance(records, emptyList())

        assert(result.isFallback) { "Should fallback when confidence ${result.confidence} < 0.3" }
    }

    @Test
    fun `enhance falls back when both models return null`() {
        every { tFLiteManager.isModelAvailable(ModelRegistry.PERIOD_LSTM) } returns true
        every { tFLiteManager.runInference(ModelRegistry.PERIOD_LSTM, any(), any()) } returns null
        every { tFLiteManager.isModelAvailable(ModelRegistry.PERIOD_LINEAR) } returns true
        every { tFLiteManager.runInference(ModelRegistry.PERIOD_LINEAR, any(), any()) } returns null

        val records = makeRecords(7)
        val result = enhancer.enhance(records, emptyList())

        assert(result.isFallback) { "Should fallback when both models return null" }
    }

    @Test
    fun `enhance returns valid date range`() {
        every { tFLiteManager.isModelAvailable(ModelRegistry.PERIOD_LINEAR) } returns true
        every { tFLiteManager.runInference(ModelRegistry.PERIOD_LINEAR, any(), any()) } returns
            floatArrayOf(0f, 0.85f)

        val records = makeRecords(4, "2026-05-01", 28, 5)
        val result = enhancer.enhance(records, emptyList())

        assert(!result.isFallback)
        assert(result.nextPeriodStart != null)
        assert(result.nextPeriodEnd != null)
        assert(result.nextPeriodStart!!.isAfter(java.time.LocalDate.parse("2026-05-28")))
    }
}
