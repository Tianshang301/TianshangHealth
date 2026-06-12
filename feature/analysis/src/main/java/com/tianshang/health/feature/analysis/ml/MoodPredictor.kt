package com.tianshang.health.feature.analysis.ml

import android.util.Log
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.database.entity.DailyHealth
import javax.inject.Inject
import javax.inject.Singleton

enum class CyclePhase {
    FOLLICULAR, LUTEAL, MENSTRUAL
}

data class MoodPredictionResult(
    val moodScore: Int,
    val stressLevel: Int,
    val rawMood: Float,
    val rawStress: Float,
    val confidence: Float,
    val isFallback: Boolean,
    val explanation: String
)

@Singleton
class MoodPredictor @Inject constructor(
    private val tFLiteManager: TFLiteManager,
    private val stringResolver: StringResolver
) {

    fun predict(
        today: DailyHealth,
        yesterday: DailyHealth?,
        cyclePhase: CyclePhase = CyclePhase.FOLLICULAR,
        dayOfCycle: Float = 1f
    ): MoodPredictionResult {
        val spec = ModelRegistry.getModelSpec(ModelRegistry.MOOD_MLP)
            ?: return fallback(stringResolver.getString(R.string.mood_model_not_registered))
        if (!tFLiteManager.isModelAvailable(ModelRegistry.MOOD_MLP)) {
            return fallback(stringResolver.getString(R.string.mood_model_not_available))
        }

        val input = extractFeatures(today, yesterday, cyclePhase, dayOfCycle)

        return try {
            val output = tFLiteManager.runInference(ModelRegistry.MOOD_MLP, input, spec)
            if (output == null || output.size < 2) {
                return fallback(stringResolver.getString(R.string.mood_inference_null))
            }

            val denormMood = output[0] * MOOD_STD + MOOD_MEAN
            val denormStress = output[1] * STRESS_STD + STRESS_MEAN

            val moodInt = denormMood.coerceIn(1f, 5f).toInt()
            val stressInt = denormStress.coerceIn(1f, 5f).toInt()

            MoodPredictionResult(
                moodScore = moodInt,
                stressLevel = stressInt,
                rawMood = denormMood,
                rawStress = denormStress,
                confidence = 0.7f,
                isFallback = false,
                explanation = stringResolver.getString(
                    R.string.mood_tflite_result,
                    ModelRegistry.MOOD_MLP,
                    moodInt,
                    stressInt
                )
            )
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
            Log.w(TAG, "Mood TFLite inference error", e)
            fallback("Inference error: ${e.message}")
        }
    }

    fun extractFeatures(
        today: DailyHealth,
        yesterday: DailyHealth?,
        cyclePhase: CyclePhase,
        dayOfCycle: Float
    ): FloatArray {
        val steps = (today.steps ?: HealthConstants.DEFAULT_STEPS).coerceAtLeast(0) / HealthConstants.STEPS_NORM_DENOMINATOR
        val exerciseMin = (today.exerciseMinutes ?: HealthConstants.DEFAULT_EXERCISE_MINUTES)
            .coerceIn(0, HealthConstants.EXERCISE_MINUTES_MAX) / HealthConstants.MINUTES_PER_HOUR
        val sleepHrs = (today.sleepHours ?: HealthConstants.DEFAULT_SLEEP_HOURS)
            .coerceIn(HealthConstants.SLEEP_HOURS_MIN, HealthConstants.SLEEP_HOURS_MAX) / HealthConstants.SLEEP_HOURS_MAX
        val deepSleep = (today.deepSleepHours ?: HealthConstants.DEFAULT_DEEP_SLEEP_HOURS)
            .coerceIn(HealthConstants.DEEP_SLEEP_MIN_HOURS, HealthConstants.DEEP_SLEEP_MAX_HOURS) / HealthConstants.DEEP_SLEEP_MAX_HOURS
        val sleepQuality = ((today.sleepQuality ?: HealthConstants.SLEEP_QUALITY_FAIR_THRESHOLD) - 1)
            .coerceIn(0, HealthConstants.SLEEP_QUALITY_NORM_MAX) / HealthConstants.SLEEP_QUALITY_NORM_MAX.toFloat()
        val caloriesIn = (today.caloriesIntake ?: HealthConstants.DEFAULT_CALORIE_INTAKE)
            .coerceAtLeast(0f) / HealthConstants.CALORIE_NORM_MAX
        val waterMl = (today.waterIntake ?: HealthConstants.DEFAULT_WATER_INTAKE_ML)
            .coerceAtLeast(0f) / HealthConstants.WATER_NORM_MAX_ML
        val heartRate = (today.heartRateResting ?: HealthConstants.DEFAULT_RESTING_HEART_RATE)
            .coerceIn(HealthConstants.RESTING_HEART_RATE_MIN, HealthConstants.RESTING_HEART_RATE_MAX) /
            HealthConstants.RESTING_HEART_RATE_MAX.toFloat()

        val prevMood = ((yesterday?.moodScore ?: today.moodScore ?: HealthConstants.MOOD_SCORE_DEFAULT) - 1)
            .coerceIn(0, HealthConstants.SLEEP_QUALITY_NORM_MAX) / HealthConstants.SLEEP_QUALITY_NORM_MAX.toFloat()
        val prevStress = ((yesterday?.stressLevel ?: today.stressLevel ?: HealthConstants.STRESS_LEVEL_DEFAULT) - 1)
            .coerceIn(0, HealthConstants.SLEEP_QUALITY_NORM_MAX) / HealthConstants.SLEEP_QUALITY_NORM_MAX.toFloat()

        val phaseFol = if (cyclePhase == CyclePhase.FOLLICULAR) 1f else 0f
        val phaseLut = if (cyclePhase == CyclePhase.LUTEAL) 1f else 0f
        val phaseMen = if (cyclePhase == CyclePhase.MENSTRUAL) 1f else 0f

        val dayOfCycleNorm = dayOfCycle.coerceIn(1f, HealthConstants.CYCLE_DAY_NORMALIZATION_MAX) /
            HealthConstants.CYCLE_DAY_NORMALIZATION_MAX

        return floatArrayOf(
            steps, exerciseMin, sleepHrs, deepSleep, sleepQuality,
            caloriesIn, waterMl, heartRate,
            prevMood, prevStress,
            phaseFol, phaseLut, phaseMen,
            dayOfCycleNorm, WEEKEND_DEFAULT
        )
    }

    private fun fallback(explanation: String): MoodPredictionResult {
        return MoodPredictionResult(
            moodScore = HealthConstants.MOOD_SCORE_DEFAULT,
            stressLevel = HealthConstants.STRESS_LEVEL_DEFAULT,
            rawMood = HealthConstants.MOOD_SCORE_DEFAULT.toFloat(),
            rawStress = HealthConstants.STRESS_LEVEL_DEFAULT.toFloat(),
            confidence = 0f,
            isFallback = true,
            explanation = explanation
        )
    }

    companion object {
        private const val TAG = "MoodPredictor"
        private const val WEEKEND_DEFAULT = 0f
        private const val MOOD_MEAN = 4.53f
        private const val MOOD_STD = 0.61f
        private const val STRESS_MEAN = 2.28f
        private const val STRESS_STD = 0.70f
    }
}
