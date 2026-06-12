package com.tianshang.health.core.common.util

import com.tianshang.health.core.common.constants.HealthConstants
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object ValidationUtils {

    fun isValidDate(date: LocalDate): Boolean {
        return !date.isAfter(LocalDate.now())
    }

    fun isValidDateRange(startDate: LocalDate, endDate: LocalDate?): Boolean {
        if (endDate == null) return true
        return !endDate.isBefore(startDate)
    }

    fun isValidFlowLevel(level: Int?): Boolean {
        if (level == null) return true
        return level in 1..3
    }

    fun isValidPainLevel(level: Int?): Boolean {
        if (level == null) return true
        return level in 0..3
    }

    fun isValidCycleLength(length: Int): Boolean {
        return length in HealthConstants.MIN_CYCLE_LENGTH..HealthConstants.MAX_CYCLE_LENGTH
    }

    fun isValidPeriodLength(length: Int): Boolean {
        return length in 2..10
    }

    fun isValidTemperature(temp: Float?): Boolean {
        if (temp == null) return true
        return temp in HealthConstants.BODY_TEMPERATURE_MIN_C..HealthConstants.BODY_TEMPERATURE_MAX_C
    }

    fun isValidWeight(weight: Float?): Boolean {
        if (weight == null) return true
        return weight in HealthConstants.MIN_WEIGHT_KG..HealthConstants.MAX_WEIGHT_KG
    }

    fun isValidHeight(height: Float?): Boolean {
        if (height == null) return true
        return height in HealthConstants.MIN_HEIGHT_CM..HealthConstants.MAX_HEIGHT_CM
    }

    fun isValidDateString(date: String): Boolean {
        return try {
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }

    fun isValidGender(gender: String): Boolean {
        return gender in listOf("male", "female", "other")
    }

    fun isValidSleepQuality(value: Int?): Boolean {
        if (value == null) return true
        return value in 1..HealthConstants.SLEEP_QUALITY_MAX.toInt()
    }

    fun isValidMoodScore(value: Int?): Boolean {
        if (value == null) return true
        return value in HealthConstants.MOOD_SCORE_MIN..HealthConstants.MOOD_SCORE_MAX
    }

    fun isValidStressLevel(value: Int?): Boolean {
        if (value == null) return true
        return value in 1..5
    }

    fun isValidHeartRate(value: Int?): Boolean {
        if (value == null) return true
        return value in HealthConstants.RESTING_HEART_RATE_MIN..HealthConstants.RESTING_HEART_RATE_MAX
    }

    fun isValidExerciseMinutes(value: Int?): Boolean {
        if (value == null) return true
        return value in 0..HealthConstants.EXERCISE_MINUTES_MAX
    }

    fun isValidId(id: Long): Boolean {
        return id > 0L
    }

    fun isValidLimit(limit: Int, maxLimit: Int = HealthConstants.DAYS_IN_YEAR.toInt()): Boolean {
        return limit > 0 && limit <= maxLimit
    }

    fun isValidNonNegative(value: Float?): Boolean {
        if (value == null) return true
        return value >= 0f
    }

    fun isValidNonNegativeInt(value: Int?): Boolean {
        if (value == null) return true
        return value >= 0
    }

    fun isValidStepCount(count: Int?): Boolean {
        if (count == null) return true
        return count >= 0
    }

    fun isValidWaterIntake(ml: Float?): Boolean {
        if (ml == null) return true
        return ml in 0f..HealthConstants.WATER_NORM_MAX_ML
    }

    fun isValidBmiCategory(category: String): Boolean {
        return category in listOf(
            "underweight", "normal", "overweight", "obese"
        )
    }

    fun isValidOvulationTestResult(result: String?): Boolean {
        if (result == null) return true
        return result in listOf("positive", "negative", "unclear")
    }

    fun isValidCervicalMucus(mucus: String?): Boolean {
        if (mucus == null) return true
        return mucus in listOf(
            "dry", "sticky", "creamy", "egg_white", "watery"
        )
    }

    fun isValidConfidenceScore(score: Float?): Boolean {
        if (score == null) return true
        return score in 0.0f..1.0f
    }

    fun isValidConfidenceLevel(level: String): Boolean {
        return level in listOf("HIGH", "MEDIUM", "LOW", "INSUFFICIENT_DATA")
    }

    fun isValidExerciseType(type: String): Boolean {
        return type.isNotBlank()
    }

    fun isValidFoodName(name: String): Boolean {
        return name.isNotBlank() && name.length <= 100
    }

    fun isValidMealType(type: String): Boolean {
        return type.isNotBlank()
    }

    fun isValidCalories(calories: Float?): Boolean {
        if (calories == null) return true
        return calories >= 0f
    }
}
