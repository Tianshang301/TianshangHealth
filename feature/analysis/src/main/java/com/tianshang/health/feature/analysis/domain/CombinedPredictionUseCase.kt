package com.tianshang.health.feature.analysis.domain

import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.database.entity.DailySymptom
import com.tianshang.health.core.database.entity.PeriodRecord
import com.tianshang.health.core.period.api.PeriodPredictionEngine
import com.tianshang.health.core.period.api.PredictionResult
import com.tianshang.health.feature.analysis.ml.EnhancedPrediction
import com.tianshang.health.feature.analysis.ml.PredictionEnhancer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class CombinedPrediction(
    val rulesPrediction: PredictionResult?,
    val tfliteEnhanced: EnhancedPrediction,
    val agreementScore: Float
)

@Singleton
class CombinedPredictionUseCase @Inject constructor(
    private val predictionEngine: PeriodPredictionEngine,
    private val predictionEnhancer: PredictionEnhancer,
    private val predictionLogger: PredictionLogger
) {
    private val logScope = CoroutineScope(Dispatchers.Default)

    fun execute(
        records: List<PeriodRecord>,
        symptoms: List<DailySymptom>,
        lutealPhaseLength: Int = DEFAULT_LUTEAL_PHASE,
        userId: Long = 0L
    ): CombinedPrediction {
        val rulesResult = predictionEngine.predict(records, symptoms, lutealPhaseLength)
        val rulesPrediction = rulesResult?.nextPeriodStart

        val enhanced = predictionEnhancer.enhance(records, symptoms, lutealPhaseLength, rulesPrediction)

        val agreement = computeAgreement(rulesResult, enhanced)

        val result = CombinedPrediction(
            rulesPrediction = rulesResult,
            tfliteEnhanced = enhanced,
            agreementScore = agreement
        )

        if (userId > 0L) {
            logScope.launch {
                predictionLogger.logCombinedPrediction(userId, result)
            }
        }

        return result
    }

    private fun computeAgreement(
        rules: PredictionResult?,
        enhanced: EnhancedPrediction
    ): Float {
        val rulesDate = rules?.nextPeriodStart ?: return 0f
        val enhancedDate = enhanced.nextPeriodStart ?: return 0f

        val diff = kotlin.math.abs(
            java.time.temporal.ChronoUnit.DAYS.between(rulesDate, enhancedDate)
        ).toFloat()

        return when {
            diff <= 1 -> HealthConstants.AGREEMENT_SCORE_EXACT
            diff <= 3 -> HealthConstants.AGREEMENT_SCORE_CLOSE
            diff <= 5 -> HealthConstants.AGREEMENT_SCORE_MODERATE
            diff <= 7 -> HealthConstants.AGREEMENT_SCORE_WEAK
            else -> HealthConstants.AGREEMENT_SCORE_NONE
        }
    }

    fun shouldTrustTflite(combined: CombinedPrediction): Boolean {
        return !combined.tfliteEnhanced.isFallback &&
            combined.tfliteEnhanced.confidence >= MIN_CONFIDENCE &&
            combined.agreementScore >= MIN_AGREEMENT
    }

    companion object {
        private const val DEFAULT_LUTEAL_PHASE = HealthConstants.DEFAULT_LUTEAL_PHASE_LENGTH
        private const val MIN_CONFIDENCE = HealthConstants.AGREEMENT_SCORE_WEAK
        private const val MIN_AGREEMENT = HealthConstants.AGREEMENT_SCORE_WEAK
    }
}
