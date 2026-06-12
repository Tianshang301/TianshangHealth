package com.tianshang.health.feature.fitness.util

import com.tianshang.health.core.common.constants.HealthConstants

object CalorieCalculator {

    private const val KCAL_TO_KJ = 4.184f

    /** Mifflin-St Jeor BMR. Age defaults to 30. */
    fun calculateBmr(weightKg: Float, heightCm: Float, isMale: Boolean, age: Int = HealthConstants.DEFAULT_AGE): Float {
        return if (isMale) {
            HealthConstants.BMR_WEIGHT_COEFFICIENT * weightKg + HealthConstants.BMR_HEIGHT_COEFFICIENT * heightCm - HealthConstants.BMR_AGE_COEFFICIENT * age + HealthConstants.BMR_MALE_OFFSET
        } else {
            HealthConstants.BMR_WEIGHT_COEFFICIENT * weightKg + HealthConstants.BMR_HEIGHT_COEFFICIENT * heightCm - HealthConstants.BMR_AGE_COEFFICIENT * age - HealthConstants.BMR_FEMALE_OFFSET
        }
    }

    /**
     * MET-based workout calories using weight only (standard formula).
     * Fallback when height is unavailable.
     */
    fun calculateKcal(
        metValue: Float,
        weightKg: Float?,
        durationMinutes: Int
    ): Float {
        val weight = weightKg ?: HealthConstants.DEFAULT_WEIGHT_KG
        val durationHours = durationMinutes / HealthConstants.MINUTES_PER_HOUR
        return metValue * weight * durationHours
    }

    /**
     * BMR-individualized MET formula using Mifflin-St Jeor.
     * Incorporates height, weight, and gender for personalized RMR estimation.
     * Falls back to standard MET × weight formula when height is unavailable or out of range.
     */
    fun calculateKcal(
        metValue: Float,
        weightKg: Float?,
        heightCm: Float?,
        isMale: Boolean,
        durationMinutes: Int
    ): Float {
        val weight = weightKg ?: HealthConstants.DEFAULT_WEIGHT_KG
        if (heightCm != null && heightCm >= HealthConstants.MIN_HEIGHT_CM && heightCm <= HealthConstants.MAX_HEIGHT_CM) {
            val bmr = calculateBmr(weight, heightCm, isMale)
            val rmrPerHour = bmr / HealthConstants.HOURS_PER_DAY
            val durationHours = durationMinutes / HealthConstants.MINUTES_PER_HOUR
            return metValue * rmrPerHour * durationHours
        }
        return calculateKcal(metValue, weightKg, durationMinutes)
    }

    fun kcalToKj(kcal: Float): Float {
        return kcal * KCAL_TO_KJ
    }

    fun formatKcalOrKj(
        kcal: Float,
        useKj: Boolean
    ): String {
        return if (useKj) {
            "${String.format("%.0f", kcalToKj(kcal))} kJ"
        } else {
            "${String.format("%.0f", kcal)} kcal"
        }
    }

    fun formatBoth(kcal: Float): Pair<String, String> {
        return Pair(
            "${String.format("%.0f", kcal)} kcal",
            "${String.format("%.0f", kcalToKj(kcal))} kJ"
        )
    }

    /** Steps-to-calories using weight only (fallback). */
    fun stepsToKcal(steps: Int, weightKg: Float?): Float {
        val weight = weightKg ?: HealthConstants.DEFAULT_WEIGHT_KG
        return steps * HealthConstants.STEP_TO_KCAL_BASE_FACTOR * (weight / HealthConstants.DEFAULT_WEIGHT_KG)
    }

    /**
     * Height-aware steps-to-calories using stride length estimation.
     * Stride ≈ height × 0.414, walking coefficient ≈ 0.7 kcal/kg/km.
     */
    fun stepsToKcal(steps: Int, weightKg: Float?, heightCm: Float?): Float {
        val weight = weightKg ?: HealthConstants.DEFAULT_WEIGHT_KG
        if (heightCm != null && heightCm >= HealthConstants.MIN_HEIGHT_CM && heightCm <= HealthConstants.MAX_HEIGHT_CM) {
            val strideLengthKm = heightCm * HealthConstants.STRIDE_LENGTH_COEFFICIENT / 100_000f
            val distanceKm = steps * strideLengthKm
            return distanceKm * weight * HealthConstants.WALKING_KCAL_PER_KG_KM
        }
        return stepsToKcal(steps, weightKg)
    }
}
