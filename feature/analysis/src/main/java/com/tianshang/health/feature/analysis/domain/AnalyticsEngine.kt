package com.tianshang.health.feature.analysis.domain

import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.database.entity.DailyHealth
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object AnalyticsEngine {

    private val dayFormatter = DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault())

    fun computeNutrition(records: List<DailyHealth>): WeeklyNutrition {
        val valid = records.filter { it.caloriesIntake != null }
        val avgCalories = if (valid.isNotEmpty()) valid.map { it.caloriesIntake!! }.average().toFloat() else 0f
        val avgProtein = if (valid.isNotEmpty()) {
            valid.filter { it.proteinGrams != null }
                .let { if (it.isNotEmpty()) it.map { p -> p.proteinGrams!! }.average().toFloat() else 0f }
        } else {
            0f
        }
        val avgCarbs = if (valid.isNotEmpty()) {
            valid.filter { it.carbsGrams != null }
                .let { if (it.isNotEmpty()) it.map { c -> c.carbsGrams!! }.average().toFloat() else 0f }
        } else {
            0f
        }
        val avgFat = if (valid.isNotEmpty()) {
            valid.filter { it.fatGrams != null }
                .let { if (it.isNotEmpty()) it.map { f -> f.fatGrams!! }.average().toFloat() else 0f }
        } else {
            0f
        }
        val avgWater = if (valid.isNotEmpty()) {
            valid.filter { it.waterIntake != null }
                .let { if (it.isNotEmpty()) it.map { w -> w.waterIntake!! }.average().toFloat() else 0f }
        } else {
            0f
        }

        return WeeklyNutrition(
            avgCalories = avgCalories,
            avgProteinGrams = avgProtein,
            avgCarbsGrams = avgCarbs,
            avgFatGrams = avgFat,
            avgWaterMl = avgWater,
            dailyCalories = records.map { DailyValue(formatDate(it.date), it.caloriesIntake ?: 0f) },
            dailyProtein = records.map { DailyValue(formatDate(it.date), it.proteinGrams ?: 0f) },
            dailyCarbs = records.map { DailyValue(formatDate(it.date), it.carbsGrams ?: 0f) },
            dailyFat = records.map { DailyValue(formatDate(it.date), it.fatGrams ?: 0f) }
        )
    }

    fun computeSleep(records: List<DailyHealth>): WeeklySleep {
        val valid = records.filter { it.sleepHours != null }
        val avgHours = if (valid.isNotEmpty()) valid.map { it.sleepHours!! }.average().toFloat() else 0f
        val avgDeep = if (valid.isNotEmpty()) {
            valid.filter { it.deepSleepHours != null }
                .let { if (it.isNotEmpty()) it.map { d -> d.deepSleepHours!! }.average().toFloat() else 0f }
        } else {
            0f
        }
        val avgQuality = if (valid.isNotEmpty()) {
            valid.filter { it.sleepQuality != null }
                .let { if (it.isNotEmpty()) it.map { q -> q.sleepQuality!!.toFloat() }.average().toFloat() else 0f }
        } else {
            0f
        }

        return WeeklySleep(
            avgHours = avgHours,
            avgDeepHours = avgDeep,
            avgQuality = avgQuality,
            dailyHours = records.map { DailyValue(formatDate(it.date), it.sleepHours ?: 0f) },
            dailyDeepHours = records.map { DailyValue(formatDate(it.date), it.deepSleepHours ?: 0f) },
            dailyQuality = records.map { DailyValue(formatDate(it.date), (it.sleepQuality ?: 0).toFloat()) }
        )
    }

    fun computeExercise(records: List<DailyHealth>): WeeklyExercise {
        val valid = records.filter { it.exerciseMinutes != null && it.exerciseMinutes!! > 0 }
        val totalMin = valid.sumOf { it.exerciseMinutes!! }
        val avgMin = if (records.isNotEmpty()) totalMin.toFloat() / records.size else 0f
        val totalCal = if (valid.isNotEmpty()) valid.map { it.caloriesBurned ?: 0f }.sum() else 0f

        val typeMap = mutableMapOf<String, Float>()
        valid.forEach { r ->
            val t = r.exerciseType ?: "Other"
            typeMap[t] = (typeMap[t] ?: 0f) + (r.caloriesBurned ?: 0f)
        }

        return WeeklyExercise(
            totalMinutes = totalMin,
            avgMinutesPerDay = avgMin,
            totalCaloriesBurned = totalCal,
            dailyMinutes = records.map { DailyValue(formatDate(it.date), (it.exerciseMinutes ?: 0).toFloat()) },
            typeDistribution = typeMap.map { TypeValue(it.key, it.value) }
        )
    }

    fun computeCalorieBalance(records: List<DailyHealth>, user: com.tianshang.health.core.database.entity.User? = null): CalorieBalance {
        val valid = records.filter { it.caloriesIntake != null }
        val avgIn = if (valid.isNotEmpty()) valid.map { it.caloriesIntake!! }.average().toFloat() else 0f
        val avgExerciseBurned = if (valid.isNotEmpty()) valid.map { it.caloriesBurned ?: 0f }.average().toFloat() else 0f

        val restingEnergy = calculateRestingEnergy(user)
        val avgTotalExpenditure = restingEnergy + avgExerciseBurned

        return CalorieBalance(
            avgCaloriesIn = avgIn,
            avgCaloriesBurned = avgExerciseBurned,
            avgRestingEnergy = restingEnergy,
            avgTotalExpenditure = avgTotalExpenditure,
            netDaily = records.map {
                val totalOut = restingEnergy + (it.caloriesBurned ?: 0f)
                Pair(formatDate(it.date), (it.caloriesIntake ?: 0f) - totalOut)
            }
        )
    }

    private fun calculateRestingEnergy(user: com.tianshang.health.core.database.entity.User?): Float {
        if (user == null) {
            return calculateBmr(
                HealthConstants.DEFAULT_WEIGHT_KG,
                HealthConstants.DEFAULT_HEIGHT_CM,
                false,
                HealthConstants.DEFAULT_AGE
            )
        }
        val weightKg = HealthConstants.DEFAULT_WEIGHT_KG
        val heightCm = user.heightCm ?: HealthConstants.DEFAULT_HEIGHT_CM
        val isMale = user.gender == "male"
        val age = calculateAge(user.dateOfBirth)
        return calculateBmr(weightKg, heightCm, isMale, age)
    }

    private fun calculateBmr(weightKg: Float, heightCm: Float, isMale: Boolean, age: Int): Float {
        return if (isMale) {
            HealthConstants.BMR_WEIGHT_COEFFICIENT * weightKg +
                HealthConstants.BMR_HEIGHT_COEFFICIENT * heightCm -
                HealthConstants.BMR_AGE_COEFFICIENT * age +
                HealthConstants.BMR_MALE_OFFSET
        } else {
            HealthConstants.BMR_WEIGHT_COEFFICIENT * weightKg +
                HealthConstants.BMR_HEIGHT_COEFFICIENT * heightCm -
                HealthConstants.BMR_AGE_COEFFICIENT * age -
                HealthConstants.BMR_FEMALE_OFFSET
        }
    }

    private fun calculateAge(dateOfBirth: String?): Int {
        if (dateOfBirth.isNullOrBlank()) return HealthConstants.DEFAULT_AGE
        return try {
            val birthDate = java.time.LocalDate.parse(dateOfBirth)
            java.time.temporal.ChronoUnit.YEARS.between(birthDate, java.time.LocalDate.now()).toInt()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (_: Exception) {
            HealthConstants.DEFAULT_AGE
        }
    }

    fun computePhaseComparisons(
        records: List<DailyHealth>,
        follicularDateRange: Pair<String, String>?,
        lutealDateRange: Pair<String, String>?
    ): List<PhaseComparison> {
        val result = mutableListOf<PhaseComparison>()

        if (follicularDateRange != null) {
            val fRecords = records.filter {
                it.date >= follicularDateRange.first && it.date <= follicularDateRange.second
            }
            result.add(
                PhaseComparison(
                    phaseNameResId = com.tianshang.health.core.common.R.string.phase_follicular,
                    sleepAvg = avgOrZero(fRecords) { it.sleepHours },
                    stepsAvg = avgOrZero(fRecords) { it.steps?.toFloat() },
                    moodAvg = avgOrZero(fRecords) { it.moodScore?.toFloat() },
                    stressAvg = avgOrZero(fRecords) { it.stressLevel?.toFloat() },
                    calorieAvg = avgOrZero(fRecords) { it.caloriesIntake }
                )
            )
        }

        if (lutealDateRange != null) {
            val lRecords = records.filter {
                it.date >= lutealDateRange.first && it.date <= lutealDateRange.second
            }
            result.add(
                PhaseComparison(
                    phaseNameResId = com.tianshang.health.core.common.R.string.phase_luteal,
                    sleepAvg = avgOrZero(lRecords) { it.sleepHours },
                    stepsAvg = avgOrZero(lRecords) { it.steps?.toFloat() },
                    moodAvg = avgOrZero(lRecords) { it.moodScore?.toFloat() },
                    stressAvg = avgOrZero(lRecords) { it.stressLevel?.toFloat() },
                    calorieAvg = avgOrZero(lRecords) { it.caloriesIntake }
                )
            )
        }

        return result
    }

    private fun avgOrZero(records: List<DailyHealth>, selector: (DailyHealth) -> Float?): Float {
        val valid = records.mapNotNull(selector)
        return if (valid.isNotEmpty()) valid.average().toFloat() else 0f
    }

    private fun formatDate(dateStr: String): String {
        return try {
            val date = LocalDate.parse(dateStr)
            date.format(dayFormatter)
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (_: Exception) {
            dateStr.takeLast(5)
        }
    }
}
