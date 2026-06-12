package com.tianshang.health.feature.analysis.domain

import com.tianshang.health.core.database.dao.PredictionLogDao
import com.tianshang.health.core.database.entity.PredictionLog
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PredictionLogger @Inject constructor(
    private val predictionLogDao: PredictionLogDao
) {

    suspend fun logCombinedPrediction(
        userId: Long,
        combined: CombinedPrediction,
        algorithmVersion: String = "v2.0"
    ): Long {
        val finalPrediction = combined.tfliteEnhanced
        val rulesDate = combined.rulesPrediction?.nextPeriodStart
        val tfliteDate = finalPrediction.nextPeriodStart
        val usedDate = tfliteDate ?: rulesDate

        val errorDays = calculateError(usedDate)
        val confidenceLabel = computeConfidenceLabel(
            finalPrediction.confidence,
            combined.rulesPrediction?.confidence
        )

        return predictionLogDao.insert(
            PredictionLog(
                userId = userId,
                predictedStartDate = usedDate?.toString() ?: "",
                predictedEndDate = finalPrediction.nextPeriodEnd?.toString(),
                actualStartDate = null,
                errorDays = errorDays,
                confidence = confidenceLabel,
                algorithmVersion = if (!finalPrediction.isFallback && finalPrediction.modelUsed != null) {
                    "$algorithmVersion+tflite"
                } else {
                    algorithmVersion
                },
                decayFactorUsed = 0.75f,
                tflitePredictedStartDate = tfliteDate?.toString(),
                rulesPredictedStartDate = rulesDate?.toString(),
                tfliteModelUsed = finalPrediction.modelUsed,
                tfliteConfidence = finalPrediction.confidence,
                agreementScore = combined.agreementScore
            )
        )
    }

    suspend fun resolvePrediction(userId: Long, actualStartDate: LocalDate) {
        val unresolved = predictionLogDao.getMostRecentTfliteUnresolved(userId)
            ?: predictionLogDao.getMostRecentUnresolved(userId) ?: return

        val error = unresolved.predictedStartDate.let {
            try {
                val predicted = LocalDate.parse(it)
                ChronoUnit.DAYS.between(predicted, actualStartDate).toInt()
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (_: Exception) { null }
        }

        predictionLogDao.update(
            unresolved.copy(
                actualStartDate = actualStartDate.toString(),
                errorDays = error,
                resolvedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getMeanAbsoluteError(userId: Long): Float? {
        return predictionLogDao.getMeanAbsoluteError(userId)
    }

    suspend fun getTfliteMeanAbsoluteError(userId: Long, modelName: String): Float? {
        return predictionLogDao.getMeanAbsoluteErrorByModel(userId, modelName)
    }

    private fun calculateError(predictedDate: LocalDate?): Int? {
        if (predictedDate == null) return null
        return ChronoUnit.DAYS.between(predictedDate, LocalDate.now()).toInt()
    }

    private fun computeConfidenceLabel(
        tfliteConfidence: Float,
        rulesConfidence: com.tianshang.health.core.period.api.Confidence?
    ): String {
        val tfliteLabel = when {
            tfliteConfidence >= 0.8f -> CONFIDENCE_HIGH
            tfliteConfidence >= 0.5f -> CONFIDENCE_MEDIUM
            tfliteConfidence >= 0.3f -> CONFIDENCE_LOW
            else -> null
        }
        if (tfliteLabel != null) return tfliteLabel
        return rulesConfidence?.name ?: CONFIDENCE_INSUFFICIENT
    }

    companion object {
        private const val CONFIDENCE_HIGH = "HIGH"
        private const val CONFIDENCE_MEDIUM = "MEDIUM"
        private const val CONFIDENCE_LOW = "LOW"
        private const val CONFIDENCE_INSUFFICIENT = "INSUFFICIENT_DATA"
    }
}
