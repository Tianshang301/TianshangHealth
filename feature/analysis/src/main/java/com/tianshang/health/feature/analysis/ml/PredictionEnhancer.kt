package com.tianshang.health.feature.analysis.ml

import android.util.Log
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.database.entity.DailySymptom
import com.tianshang.health.core.database.entity.PeriodRecord
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class EnhancedPrediction(
    val nextPeriodStart: LocalDate?,
    val nextPeriodEnd: LocalDate?,
    val confidence: Float,
    val modelUsed: String?,
    val isFallback: Boolean,
    val explanation: String
)

@Singleton
class PredictionEnhancer @Inject constructor(
    private val tFLiteManager: TFLiteManager,
    private val featureExtractor: FeatureExtractor,
    private val stringResolver: StringResolver
) {

    fun enhance(
        records: List<PeriodRecord>,
        symptoms: List<DailySymptom>,
        lutealPhaseLength: Int = DEFAULT_LUTEAL_PHASE,
        rulesEnginePrediction: LocalDate? = null
    ): EnhancedPrediction {
        val features = featureExtractor.extractPeriodFeatures(records, symptoms, lutealPhaseLength)

        if (features == null || features.cycleLengths.size < ModelRegistry.MIN_CYCLES_FOR_PREDICTION) {
            return EnhancedPrediction(
                nextPeriodStart = null,
                nextPeriodEnd = null,
                confidence = 0f,
                modelUsed = null,
                isFallback = true,
                explanation = stringResolver.getString(
                    R.string.prediction_insufficient_cycles,
                    ModelRegistry.MIN_CYCLES_FOR_PREDICTION
                )
            )
        }

        val result = try {
            var best: EnhancedPrediction? = null
            if (features.cycleLengths.size >= ModelRegistry.MIN_CYCLES_FOR_LSTM) {
                best = runLstmInference(features, records, lutealPhaseLength)
            }
            if (best == null && features.cycleLengths.size >= ModelRegistry.MIN_CYCLES_FOR_LINEAR) {
                best = runLinearInference(features, records, lutealPhaseLength)
            }
            best
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
            Log.w(TAG, "TFLite inference error", e)
            null
        }

        if (result != null && result.confidence >= ModelRegistry.CONFIDENCE_THRESHOLD) {
            return result
        }

        return fallbackToRulesEngine(records, features, rulesEnginePrediction)
    }

    private fun runLstmInference(
        features: FeatureExtractor.PeriodFeatures,
        records: List<PeriodRecord>,
        lutealPhaseLength: Int
    ): EnhancedPrediction? {
        val spec = ModelRegistry.getModelSpec(ModelRegistry.PERIOD_LSTM) ?: return null
        if (!tFLiteManager.isModelAvailable(ModelRegistry.PERIOD_LSTM)) return null

        val input = featureExtractor.normalizeForLstm(features, spec)
        val output = tFLiteManager.runInference(ModelRegistry.PERIOD_LSTM, input.array, spec)

        return processOutput(output, records, features, spec, ModelRegistry.PERIOD_LSTM, lutealPhaseLength)
    }

    private fun runLinearInference(
        features: FeatureExtractor.PeriodFeatures,
        records: List<PeriodRecord>,
        lutealPhaseLength: Int
    ): EnhancedPrediction? {
        val spec = ModelRegistry.getModelSpec(ModelRegistry.PERIOD_LINEAR) ?: return null
        if (!tFLiteManager.isModelAvailable(ModelRegistry.PERIOD_LINEAR)) return null

        val input = featureExtractor.normalizeForLinear(features, spec)
        val output = tFLiteManager.runInference(ModelRegistry.PERIOD_LINEAR, input.array, spec)

        @Suppress("UNUSED_PARAMETER")
        return processOutput(output, records, features, spec, ModelRegistry.PERIOD_LINEAR, lutealPhaseLength)
    }

    private fun processOutput(
        output: FloatArray?,
        records: List<PeriodRecord>,
        features: FeatureExtractor.PeriodFeatures,
        spec: ModelSpec,
        modelName: String,
        @Suppress("UNUSED_PARAMETER") lutealPhaseLength: Int
    ): EnhancedPrediction? {
        if (output == null || output.size < 2) return null

        val lastRecord = records.sortedBy { it.startDate }.last()
        val lastStartDate = LocalDate.parse(lastRecord.startDate)

        val predictedDays = featureExtractor.denormalizePrediction(
            output[0],
            spec.outputMean,
            spec.outputStd
        ).toInt().coerceIn(MIN_CYCLE_LENGTH, MAX_CYCLE_LENGTH)

        val confidence = computeConfidence(features)
        val nextStart = lastStartDate.plusDays(predictedDays.toLong())
        val nextEnd = nextStart.plusDays(
            features.periodLengths.average().toInt().coerceAtLeast(1).toLong() - 1
        )

        val avgCycle = features.cycleLengths.average().toInt()
        val explanation = stringResolver.getString(
            R.string.prediction_tflite_result,
            modelName,
            predictedDays,
            avgCycle,
            (confidence * 100).toInt()
        )

        return EnhancedPrediction(
            nextPeriodStart = nextStart,
            nextPeriodEnd = nextEnd,
            confidence = confidence,
            modelUsed = modelName,
            isFallback = false,
            explanation = explanation
        )
    }

    private fun computeConfidence(features: FeatureExtractor.PeriodFeatures): Float {
        val count = features.cycleLengths.size
        if (count < ModelRegistry.MIN_CYCLES_FOR_PREDICTION) return 0f
        val avg = features.cycleLengths.average()
        val variance = features.cycleLengths.map { (it - avg) * (it - avg) }.average()
        val stdDev = kotlin.math.sqrt(variance).toFloat()
        val regularityScore = (1f - stdDev / MAX_CYCLE_LENGTH).coerceIn(0f, 1f)
        val countScore = (count.toFloat() / MAX_CONFIDENCE_CYCLES).coerceIn(0f, 1f)
        return (regularityScore * REGULARITY_WEIGHT + countScore * COUNT_WEIGHT).coerceIn(0f, 1f)
    }

    private fun fallbackToRulesEngine(
        records: List<PeriodRecord>,
        features: FeatureExtractor.PeriodFeatures,
        rulesEnginePrediction: LocalDate?
    ): EnhancedPrediction {
        val sorted = records.sortedBy { it.startDate }
        val lastStartDate = LocalDate.parse(sorted.last().startDate)

        val nextStart = rulesEnginePrediction
            ?: lastStartDate.plusDays(features.cycleLengths.average().toLong())

        val nextEnd = nextStart.plusDays(
            features.periodLengths.average().toInt().coerceAtLeast(1).toLong() - 1
        )

        val avgCycle = features.cycleLengths.average().toInt()
        val engineName = if (rulesEnginePrediction != null) "PredictionEngine" else "simple_average"
        val explanation = if (rulesEnginePrediction != null) {
            stringResolver.getString(R.string.prediction_fallback_with_engine, avgCycle, engineName)
        } else {
            stringResolver.getString(R.string.prediction_fallback_simple_average, avgCycle)
        }

        return EnhancedPrediction(
            nextPeriodStart = nextStart,
            nextPeriodEnd = nextEnd,
            confidence = HealthConstants.FALLBACK_PREDICTION_CONFIDENCE,
            modelUsed = engineName,
            isFallback = true,
            explanation = explanation
        )
    }

    companion object {
        private const val TAG = "PredictionEnhancer"
        private const val DEFAULT_LUTEAL_PHASE = HealthConstants.DEFAULT_LUTEAL_PHASE_LENGTH
        private const val MIN_CYCLE_LENGTH = HealthConstants.MIN_CYCLE_LENGTH
        private const val MAX_CYCLE_LENGTH = HealthConstants.MAX_CYCLE_LENGTH
        private const val MAX_CONFIDENCE_CYCLES = 12f
        private const val REGULARITY_WEIGHT = 0.4f
        private const val COUNT_WEIGHT = 0.6f
    }
}
