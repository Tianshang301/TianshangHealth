package com.tianshang.health.feature.analysis.domain

import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.database.entity.DailyHealth
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Cross-dimensional analysis engine.
 * Discovers correlations, patterns, and composite health scores
 * across multiple health dimensions without requiring ML frameworks.
 */
object CrossDimensionEngine {

    /**
     * Compute correlation matrix for all available dimension pairs.
     * Only includes pairs with sufficient data (>= 7 days).
     */
    fun computeCorrelationMatrix(records: List<DailyHealth>): CorrelationMatrix {
        val dimensions = extractDimensionSeries(records)
        val pairs = mutableListOf<CorrelationPair>()

        val keys = dimensions.keys.toList()
        for (i in keys.indices) {
            for (j in i + 1 until keys.size) {
                val dimA = keys[i]
                val dimB = keys[j]
                val seriesA = dimensions[dimA] ?: continue
                val seriesB = dimensions[dimB] ?: continue

                val paired = seriesA.zip(seriesB)
                    .filter { it.first != null && it.second != null }
                    .map { it.first!! to it.second!! }

                if (paired.size < 7) continue

                val correlation = pearsonCorrelation(
                    paired.map { it.first },
                    paired.map { it.second }
                ) ?: continue

                pairs.add(
                    CorrelationPair(
                        dimensionA = dimA,
                        dimensionB = dimB,
                        correlation = correlation,
                        sampleSize = paired.size,
                        interpretation = interpretCorrelation(abs(correlation))
                    )
                )
            }
        }

        return CorrelationMatrix(pairs.sortedByDescending { abs(it.correlation) })
    }

    /**
     * Calculate Health Composite Score (HCS) based on user's own percentiles.
     * Score ranges from 0-100 for each dimension.
     */
    fun computeHealthCompositeScore(
        currentRecords: List<DailyHealth>,
        historicalRecords: List<DailyHealth>
    ): HealthCompositeScore? {
        if (historicalRecords.size < 14 || currentRecords.isEmpty()) return null

        val sleepScore = calculatePercentileScore(
            currentRecords.mapNotNull { it.sleepHours },
            historicalRecords.mapNotNull { it.sleepHours }
        ) { current, historical ->
            historical.count { current >= it.toDouble() }.toDouble() / historical.size
        }

        val nutritionScore = calculatePercentileScore(
            currentRecords.mapNotNull { it.caloriesIntake },
            historicalRecords.mapNotNull { it.caloriesIntake }
        ) { current, historical ->
            // Score based on consistency (closer to median is better for calories)
            val median = historical.median()
            if (median == 0.0) return@calculatePercentileScore 0.5
            val deviation = abs(current - median) / median
            1.0 - deviation.coerceIn(0.0, 1.0)
        }

        val activityScore = calculatePercentileScore(
            currentRecords.mapNotNull { it.steps?.toFloat() },
            historicalRecords.mapNotNull { it.steps?.toFloat() }
        ) { current, historical ->
            historical.count { current >= it.toDouble() }.toDouble() / historical.size
        }

        val moodScore = calculatePercentileScore(
            currentRecords.mapNotNull { it.moodScore?.toFloat() },
            historicalRecords.mapNotNull { it.moodScore?.toFloat() }
        ) { current, historical ->
            historical.count { current >= it.toDouble() }.toDouble() / historical.size
        }

        val scores = listOfNotNull(sleepScore, nutritionScore, activityScore, moodScore)
        val overallScore = if (scores.isNotEmpty()) scores.average().toInt() else 50

        return HealthCompositeScore(
            overallScore = overallScore.coerceIn(0, 100),
            sleepScore = sleepScore?.toInt()?.coerceIn(0, 100) ?: 50,
            nutritionScore = nutritionScore?.toInt()?.coerceIn(0, 100) ?: 50,
            activityScore = activityScore?.toInt()?.coerceIn(0, 100) ?: 50,
            moodScore = moodScore?.toInt()?.coerceIn(0, 100) ?: 50,
            percentileVsSelf = scores.average().toFloat()
        )
    }

    /**
     * Detect compound health patterns by combining multiple dimensions.
     */
    fun detectCompoundPatterns(records: List<DailyHealth>): List<CompoundPattern> {
        if (records.size < 7) return emptyList()

        val patterns = mutableListOf<CompoundPattern>()

        val avgSleep = records.mapNotNull { it.sleepHours }.average()
        val avgStress = records.mapNotNull { it.stressLevel?.toFloat() }.average()
        val avgMood = records.mapNotNull { it.moodScore?.toFloat() }.average()
        val avgSteps = records.mapNotNull { it.steps?.toFloat() }.average()
        val avgCalories = records.mapNotNull { it.caloriesIntake }.average()
        val avgBurned = records.mapNotNull { it.caloriesBurned }.average()
        val totalExercise = records.sumOf { it.exerciseMinutes ?: 0 }
        val avgExercise = if (records.isNotEmpty()) totalExercise.toFloat() / records.size else 0f

        // Pattern 1: Physical exhaustion syndrome
        if (avgSleep < HealthConstants.SLEEP_DEPRIVATION_THRESHOLD_HRS && avgStress > HealthConstants.STRESS_HIGH_THRESHOLD && avgMood < HealthConstants.MOOD_LOW_THRESHOLD) {
            patterns.add(
                CompoundPattern(
                    patternId = "exhaustion",
                    titleResId = R.string.pattern_exhaustion_title,
                    descriptionResId = R.string.pattern_exhaustion_desc,
                    severity = PatternSeverity.ALERT,
                    contributingDimensions = listOf("sleep", "stress", "mood")
                )
            )
        }

        // Pattern 2: Overtraining risk
        if (avgExercise > HealthConstants.EXERCISE_MINUTES_OVERTRAINING_THRESHOLD && (avgCalories - avgBurned) < HealthConstants.CALORIE_DEFICIT_OVERTRAINING_THRESHOLD && avgSleep < HealthConstants.SLEEP_LOW_THRESHOLD_HRS) {
            patterns.add(
                CompoundPattern(
                    patternId = "overtraining",
                    titleResId = R.string.pattern_overtraining_title,
                    descriptionResId = R.string.pattern_overtraining_desc,
                    severity = PatternSeverity.WARNING,
                    contributingDimensions = listOf("exercise", "calories", "sleep")
                )
            )
        }

        // Pattern 3: Sedentary + low mood
        if (avgSteps < HealthConstants.SEDENTARY_STEPS_THRESHOLD && avgMood < HealthConstants.MOOD_LOW_THRESHOLD) {
            patterns.add(
                CompoundPattern(
                    patternId = "sedentary_mood",
                    titleResId = R.string.pattern_sedentary_mood_title,
                    descriptionResId = R.string.pattern_sedentary_mood_desc,
                    severity = PatternSeverity.WARNING,
                    contributingDimensions = listOf("steps", "mood")
                )
            )
        }

        // Pattern 4: Hydration + stress
        val avgWater = records.mapNotNull { it.waterIntake }.average()
        if (avgWater < HealthConstants.WATER_LOW_THRESHOLD_ML && avgStress > HealthConstants.STRESS_HIGH_THRESHOLD) {
            patterns.add(
                CompoundPattern(
                    patternId = "dehydration_stress",
                    titleResId = R.string.pattern_dehydration_stress_title,
                    descriptionResId = R.string.pattern_dehydration_stress_desc,
                    severity = PatternSeverity.INFO,
                    contributingDimensions = listOf("water", "stress")
                )
            )
        }

        // Pattern 5: Sleep + activity positive loop
        if (avgSleep > HealthConstants.SLEEP_GOOD_THRESHOLD_HRS && avgSteps > HealthConstants.ACTIVE_STEPS_THRESHOLD && avgMood > HealthConstants.MOOD_GOOD_THRESHOLD) {
            patterns.add(
                CompoundPattern(
                    patternId = "positive_loop",
                    titleResId = R.string.pattern_positive_loop_title,
                    descriptionResId = R.string.pattern_positive_loop_desc,
                    severity = PatternSeverity.INFO,
                    contributingDimensions = listOf("sleep", "steps", "mood")
                )
            )
        }

        return patterns
    }

    /**
     * Extract time series for each health dimension.
     */
    private fun extractDimensionSeries(records: List<DailyHealth>): Map<String, List<Float?>> {
        return mapOf(
            "steps" to records.map { it.steps?.toFloat() },
            "sleep_hours" to records.map { it.sleepHours },
            "sleep_quality" to records.map { it.sleepQuality?.toFloat() },
            "deep_sleep" to records.map { it.deepSleepHours },
            "calories_intake" to records.map { it.caloriesIntake },
            "calories_burned" to records.map { it.caloriesBurned },
            "water_intake" to records.map { it.waterIntake },
            "protein" to records.map { it.proteinGrams },
            "mood" to records.map { it.moodScore?.toFloat() },
            "stress" to records.map { it.stressLevel?.toFloat() },
            "exercise_minutes" to records.map { it.exerciseMinutes?.toFloat() },
            "heart_rate" to records.map { it.heartRateResting?.toFloat() },
            "body_temperature" to records.map { it.bodyTemperature },
            "weight" to records.map { it.weightKg }
        )
    }

    private fun pearsonCorrelation(x: List<Float>, y: List<Float>): Float? {
        if (x.size != y.size || x.size < 3) return null

        val xMean = x.average()
        val yMean = y.average()

        val numerator = x.zip(y).sumOf { (xi, yi) ->
            (xi - xMean) * (yi - yMean).toDouble()
        }

        val xDenom = sqrt(x.sumOf { (it - xMean) * (it - xMean).toDouble() })
        val yDenom = sqrt(y.sumOf { (it - yMean) * (it - yMean).toDouble() })

        if (xDenom == 0.0 || yDenom == 0.0) return null

        return (numerator / (xDenom * yDenom)).toFloat()
    }

    private fun interpretCorrelation(absR: Float): CorrelationInterpretation {
        return when {
            absR < 0.2f -> CorrelationInterpretation.VERY_WEAK
            absR < 0.4f -> CorrelationInterpretation.WEAK
            absR < 0.6f -> CorrelationInterpretation.MODERATE
            absR < 0.8f -> CorrelationInterpretation.STRONG
            else -> CorrelationInterpretation.VERY_STRONG
        }
    }

    private fun calculatePercentileScore(
        currentValues: List<Float>,
        historicalValues: List<Float>,
        scorer: (Double, List<Double>) -> Double
    ): Double? {
        if (currentValues.isEmpty() || historicalValues.size < 7) return null

        val currentAvg = currentValues.average()
        return scorer(currentAvg, historicalValues.map { it.toDouble() }) * 100
    }

    private fun List<Double>.median(): Double {
        val sorted = this.sorted()
        return if (sorted.size % 2 == 0) {
            (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2
        } else {
            sorted[sorted.size / 2]
        }
    }
}
