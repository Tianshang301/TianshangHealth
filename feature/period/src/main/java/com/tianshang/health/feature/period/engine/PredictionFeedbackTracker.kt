package com.tianshang.health.feature.period.engine

import com.tianshang.health.core.database.dao.PredictionLogDao
import com.tianshang.health.core.database.entity.PredictionLog
import com.tianshang.health.core.period.api.Confidence
import com.tianshang.health.feature.period.engine.PredictionEngine.Companion.DECAY_FACTOR
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks prediction accuracy over time and adapts algorithm parameters.
 * Provides feedback loop for the PredictionEngine.
 */
@Singleton
class PredictionFeedbackTracker @Inject constructor(
    private val predictionLogDao: PredictionLogDao
) {

    companion object {
        private const val MIN_RESOLVED_PREDICTIONS = 3
        private const val TARGET_MAE = 1.5
        private const val DECAY_ADJUSTMENT_RATE = 0.05f
        private const val MIN_DECAY_FACTOR = 0.65f
        private const val MAX_DECAY_FACTOR = 0.90f
    }

    /**
     * Log a new prediction when PredictionEngine produces one.
     */
    suspend fun logPrediction(
        userId: Long,
        predictedStartDate: LocalDate,
        predictedEndDate: LocalDate?,
        confidence: Confidence
    ): Long {
        return predictionLogDao.insert(
            PredictionLog(
                userId = userId,
                predictedStartDate = predictedStartDate.toString(),
                predictedEndDate = predictedEndDate?.toString(),
                actualStartDate = null,
                errorDays = null,
                confidence = confidence.name,
                decayFactorUsed = DECAY_FACTOR.toFloat()
            )
        )
    }

    /**
     * Resolve the most recent unresolved prediction when user records an actual period start.
     */
    suspend fun resolvePrediction(userId: Long, actualStartDate: LocalDate) {
        val unresolved = predictionLogDao.getMostRecentUnresolved(userId) ?: return

        val predicted = LocalDate.parse(unresolved.predictedStartDate)
        val errorDays = ChronoUnit.DAYS.between(predicted, actualStartDate).toInt()

        predictionLogDao.update(
            unresolved.copy(
                actualStartDate = actualStartDate.toString(),
                errorDays = errorDays,
                resolvedAt = System.currentTimeMillis()
            )
        )
    }

    /**
     * Calculate Mean Absolute Error over the last N resolved predictions.
     */
    suspend fun getMeanAbsoluteError(userId: Long, lastN: Int = 6): Float? {
        val logs = predictionLogDao.getRecentResolved(userId, lastN)
        if (logs.size < MIN_RESOLVED_PREDICTIONS) return null

        return logs.mapNotNull { it.errorDays?.let { days -> kotlin.math.abs(days) } }
            .average()
            .toFloat()
    }

    /**
     * Calculate percentage of predictions within given day threshold.
     */
    suspend fun getAccuracyPercent(userId: Long, thresholdDays: Int = 2): Int? {
        val total = predictionLogDao.getResolvedCount(userId)
        if (total < MIN_RESOLVED_PREDICTIONS) return null

        val accurate = predictionLogDao.getAccuracyWithinThreshold(userId, thresholdDays)
        return (accurate * 100 / total)
    }

    /**
     * Adapt the decay factor based on historical prediction accuracy.
     * Called periodically (e.g., monthly) or when sufficient new data accumulated.
     */
    suspend fun adaptDecayFactor(userId: Long): Float {
        val mae = getMeanAbsoluteError(userId) ?: return DECAY_FACTOR.toFloat()
        val logs = predictionLogDao.getRecentResolved(userId, 6)
        if (logs.size < MIN_RESOLVED_PREDICTIONS) return DECAY_FACTOR.toFloat()

        val currentDecay = logs.lastOrNull()?.decayFactorUsed ?: DECAY_FACTOR.toFloat()

        return when {
            mae > TARGET_MAE + 0.5 -> {
                // Predictions are off; reduce weight on recent cycles (more smoothing)
                (currentDecay + DECAY_ADJUSTMENT_RATE).coerceIn(MIN_DECAY_FACTOR, MAX_DECAY_FACTOR)
            }
            mae < TARGET_MAE - 0.5 -> {
                // Predictions are accurate; increase weight on recent cycles
                (currentDecay - DECAY_ADJUSTMENT_RATE).coerceIn(MIN_DECAY_FACTOR, MAX_DECAY_FACTOR)
            }
            else -> currentDecay
        }
    }

    /**
     * Get user-facing prediction accuracy summary.
     */
    suspend fun getAccuracySummary(userId: Long): AccuracySummary? {
        val mae = getMeanAbsoluteError(userId) ?: return null
        val within2Days = getAccuracyPercent(userId, 2) ?: return null
        val total = predictionLogDao.getResolvedCount(userId)

        return AccuracySummary(
            meanAbsoluteError = mae,
            accuracyWithin2Days = within2Days,
            totalResolvedPredictions = total
        )
    }

    data class AccuracySummary(
        val meanAbsoluteError: Float,
        val accuracyWithin2Days: Int,
        val totalResolvedPredictions: Int
    )
}
