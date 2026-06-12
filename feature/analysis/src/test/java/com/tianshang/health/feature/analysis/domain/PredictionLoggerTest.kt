package com.tianshang.health.feature.analysis.domain

import com.tianshang.health.core.database.dao.PredictionLogDao
import com.tianshang.health.core.database.entity.PredictionLog
import com.tianshang.health.core.period.api.Confidence
import com.tianshang.health.core.period.api.PredictionResult
import com.tianshang.health.feature.analysis.ml.EnhancedPrediction
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class PredictionLoggerTest {

    private lateinit var dao: PredictionLogDao
    private lateinit var logger: PredictionLogger

    @Before
    fun setUp() {
        dao = mockk(relaxed = true)
        logger = PredictionLogger(dao)
    }

    @Test
    fun `logCombinedPrediction inserts with tflite fields`() = runTest {
        val combined = makeCombinedPrediction(
            tfliteDate = LocalDate.of(2026, 7, 5),
            rulesDate = LocalDate.of(2026, 7, 4),
            modelUsed = "period_linear",
            confidence = 0.85f,
            agreement = 0.8f,
            isFallback = false
        )

        logger.logCombinedPrediction(userId = 1L, combined)

        coVerify {
            dao.insert(
                match { log ->
                    log.userId == 1L &&
                        log.predictedStartDate == "2026-07-05" &&
                        log.tflitePredictedStartDate == "2026-07-05" &&
                        log.rulesPredictedStartDate == "2026-07-04" &&
                        log.tfliteModelUsed == "period_linear" &&
                        log.tfliteConfidence == 0.85f &&
                        log.agreementScore == 0.8f &&
                        log.confidence == "HIGH"
                }
            )
        }
    }

    @Test
    fun `logCombinedPrediction fallback sets default predictedStartDate`() = runTest {
        val combined = makeCombinedPrediction(
            tfliteDate = null,
            rulesDate = null,
            modelUsed = null,
            confidence = 0f,
            agreement = 0f,
            isFallback = true
        )

        logger.logCombinedPrediction(userId = 1L, combined)

        coVerify {
            dao.insert(
                match { log ->
                    log.userId == 1L &&
                        log.tfliteModelUsed == null &&
                        log.tfliteConfidence == 0f &&
                        log.confidence == "INSUFFICIENT_DATA" &&
                        log.isFallback()
                }
            )
        }
    }

    @Test
    fun `resolvePrediction updates unresolved prediction`() = runTest {
        val unresolved = PredictionLog(
            id = 1L,
            userId = 1L,
            predictedStartDate = "2026-07-05",
            predictedEndDate = null,
            actualStartDate = null,
            errorDays = null,
            confidence = "HIGH",
            tfliteModelUsed = "period_linear",
            tfliteConfidence = 0.85f
        )

        coEvery { dao.getMostRecentTfliteUnresolved(1L) } returns unresolved

        logger.resolvePrediction(userId = 1L, actualStartDate = LocalDate.of(2026, 7, 3))

        coVerify {
            dao.update(
                match { log ->
                    log.id == 1L &&
                        log.actualStartDate == "2026-07-03" &&
                        log.errorDays == -2 &&
                        log.resolvedAt != null
                }
            )
        }
    }

    @Test
    fun `resolvePrediction does nothing when no unresolved log`() = runTest {
        coEvery { dao.getMostRecentTfliteUnresolved(1L) } returns null
        coEvery { dao.getMostRecentUnresolved(1L) } returns null

        logger.resolvePrediction(userId = 1L, actualStartDate = LocalDate.of(2026, 7, 3))

        coVerify(inverse = true) { dao.update(any()) }
    }

    @Test
    fun `resolvePrediction falls back to non-tflite unresolved when no tflite log`() = runTest {
        val unresolved = PredictionLog(
            id = 2L,
            userId = 1L,
            predictedStartDate = "2026-07-04",
            predictedEndDate = null,
            actualStartDate = null,
            errorDays = null,
            confidence = "MEDIUM",
            tfliteModelUsed = null
        )

        coEvery { dao.getMostRecentTfliteUnresolved(1L) } returns null
        coEvery { dao.getMostRecentUnresolved(1L) } returns unresolved

        logger.resolvePrediction(userId = 1L, actualStartDate = LocalDate.of(2026, 7, 4))

        coVerify {
            dao.update(
                match { log ->
                    log.id == 2L &&
                        log.actualStartDate == "2026-07-04" &&
                        log.errorDays == 0
                }
            )
        }
    }

    @Test
    fun `getMeanAbsoluteError delegates to dao`() = runTest {
        coEvery { dao.getMeanAbsoluteError(1L) } returns 2.5f

        val result = logger.getMeanAbsoluteError(1L)

        assert(result == 2.5f)
        coVerify { dao.getMeanAbsoluteError(1L) }
    }

    @Test
    fun `getTfliteMeanAbsoluteError delegates to dao`() = runTest {
        coEvery { dao.getMeanAbsoluteErrorByModel(1L, "period_linear") } returns 1.8f

        val result = logger.getTfliteMeanAbsoluteError(1L, "period_linear")

        assert(result == 1.8f)
        coVerify { dao.getMeanAbsoluteErrorByModel(1L, "period_linear") }
    }

    @Test
    fun `logCombinedPrediction with low tflite confidence uses rules confidence`() = runTest {
        val combined = makeCombinedPrediction(
            tfliteDate = LocalDate.of(2026, 7, 5),
            rulesDate = LocalDate.of(2026, 7, 4),
            modelUsed = "period_linear",
            confidence = 0.2f,
            agreement = 0.5f,
            isFallback = false,
            rulesConfidence = Confidence.MEDIUM
        )

        logger.logCombinedPrediction(userId = 1L, combined)

        coVerify {
            dao.insert(
                match { log ->
                    log.confidence == "MEDIUM" &&
                        log.tfliteConfidence == 0.2f
                }
            )
        }
    }

    private fun makeCombinedPrediction(
        tfliteDate: LocalDate?,
        rulesDate: LocalDate?,
        modelUsed: String?,
        confidence: Float,
        agreement: Float,
        isFallback: Boolean,
        rulesConfidence: Confidence = Confidence.HIGH
    ): CombinedPrediction {
        val enhanced = EnhancedPrediction(
            nextPeriodStart = tfliteDate,
            nextPeriodEnd = tfliteDate?.plusDays(4),
            confidence = confidence,
            modelUsed = modelUsed,
            isFallback = isFallback,
            explanation = "test"
        )
        val rulesResult = if (rulesDate != null) {
            PredictionResult(
                nextPeriodStart = rulesDate,
                nextPeriodEnd = rulesDate.plusDays(4),
                ovulationDate = rulesDate.minusDays(14),
                fertileWindowStart = rulesDate.minusDays(19),
                fertileWindowEnd = rulesDate.minusDays(10),
                cycleLength = 28,
                periodLength = 5,
                confidence = rulesConfidence,
                explanation = "rules test"
            )
        } else {
            null
        }

        return CombinedPrediction(
            rulesPrediction = rulesResult,
            tfliteEnhanced = enhanced,
            agreementScore = agreement
        )
    }

    private fun PredictionLog.isFallback(): Boolean {
        return predictedStartDate.isEmpty() &&
            tfliteModelUsed == null &&
            tfliteConfidence != null && tfliteConfidence == 0f
    }
}
