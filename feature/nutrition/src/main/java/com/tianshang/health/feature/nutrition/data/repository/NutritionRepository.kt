package com.tianshang.health.feature.nutrition.data.repository

import android.util.Log
import androidx.annotation.StringRes
import com.tianshang.health.core.common.constants.CyclePhase
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.common.util.ValidationUtils
import com.tianshang.health.core.database.dao.DailyHealthDao
import com.tianshang.health.core.database.dao.MealDao
import com.tianshang.health.core.database.dao.PeriodRecordDao
import com.tianshang.health.core.database.dao.UserDao
import com.tianshang.health.core.database.entity.MealRecord
import com.tianshang.health.core.database.entity.User
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

data class CycleNutritionRecommendation(
    val currentPhase: CyclePhase,
    val phaseDayRange: String,
    @StringRes val focusNutrientsResId: Int,
    @StringRes val recommendedFoodsResId: Int,
    @StringRes val foodsToLimitResId: Int,
    @StringRes val generalAdviceResId: Int
)

@Singleton
class NutritionRepository @Inject constructor(
    private val mealDao: MealDao,
    private val userDao: UserDao,
    private val dailyHealthDao: DailyHealthDao,
    private val periodRecordDao: PeriodRecordDao
) {
    private var currentUser: User? = null

    suspend fun initialize() {
        if (currentUser == null) {
            currentUser = userDao.getFirst()
        }
    }

    private suspend fun requireUserId(): Long {
        val user = currentUser ?: userDao.getFirst()
        currentUser = user
        return user?.id ?: throw IllegalStateException("No user found. Onboarding must be completed first.")
    }

    suspend fun addMeal(
        mealType: String,
        foodName: String,
        calories: Float? = null,
        proteinGrams: Float? = null,
        carbsGrams: Float? = null,
        fatGrams: Float? = null,
        servingSize: String? = null,
        notes: String? = null,
        date: String = LocalDate.now().toString()
    ): Long {
        require(ValidationUtils.isValidMealType(mealType)) { "Invalid mealType: $mealType" }
        require(ValidationUtils.isValidFoodName(foodName)) { "Invalid foodName: $foodName" }
        require(ValidationUtils.isValidCalories(calories)) { "Invalid calories: $calories" }
        require(ValidationUtils.isValidDateString(date)) { "Invalid date: $date" }
        val userId = requireUserId()
        val id = mealDao.insert(
            MealRecord(
                userId = userId,
                date = date,
                mealType = mealType,
                foodName = foodName,
                calories = calories,
                proteinGrams = proteinGrams,
                carbsGrams = carbsGrams,
                fatGrams = fatGrams,
                servingSize = servingSize,
                notes = notes
            )
        )
        syncDailyHealth(date)
        return id
    }

    suspend fun updateMeal(record: MealRecord) {
        require(ValidationUtils.isValidId(record.id)) { "Invalid meal id: ${record.id}" }
        require(ValidationUtils.isValidMealType(record.mealType)) { "Invalid mealType: ${record.mealType}" }
        require(ValidationUtils.isValidFoodName(record.foodName)) { "Invalid foodName: ${record.foodName}" }
        mealDao.update(record)
        syncDailyHealth(record.date)
    }

    suspend fun deleteMeal(record: MealRecord) {
        require(ValidationUtils.isValidId(record.id)) { "Invalid meal id: ${record.id}" }
        mealDao.delete(record)
        syncDailyHealth(record.date)
    }

    fun getMealsByDate(date: String): Flow<List<MealRecord>> {
        val userId = currentUser?.id ?: return kotlinx.coroutines.flow.emptyFlow()
        return mealDao.getByDate(userId, date)
    }

    fun getAllMeals(): Flow<List<MealRecord>> {
        val userId = currentUser?.id ?: return kotlinx.coroutines.flow.emptyFlow()
        return mealDao.getAllByUser(userId)
    }

    suspend fun getDailyCalories(date: String): Float {
        val userId = requireUserId()
        return mealDao.getDailyCalories(userId, date) ?: 0f
    }

    suspend fun getDailyProtein(date: String): Float {
        val userId = requireUserId()
        return mealDao.getDailyProtein(userId, date) ?: 0f
    }

    suspend fun getDailyCarbs(date: String): Float {
        val userId = requireUserId()
        return mealDao.getDailyCarbs(userId, date) ?: 0f
    }

    suspend fun getDailyFat(date: String): Float {
        val userId = requireUserId()
        return mealDao.getDailyFat(userId, date) ?: 0f
    }

    suspend fun getWeeklyCalories(): List<Pair<String, Float>> {
        val userId = requireUserId()
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        val meals = mealDao.getByDateRange(userId, startOfWeek.toString(), today.toString())
        return meals.groupBy { it.date }
            .mapValues { (_, meals) -> meals.sumOf { it.calories?.toDouble() ?: 0.0 }.toFloat() }
            .toList()
            .sortedBy { it.first }
    }

    suspend fun getMealCountToday(): Int {
        val userId = requireUserId()
        val today = LocalDate.now().toString()
        return mealDao.getByDateOnce(userId, today).size
    }

    suspend fun getDailyWaterIntake(date: String = LocalDate.now().toString()): Float {
        val userId = requireUserId()
        return dailyHealthDao.getByDate(userId, date)?.waterIntake ?: 0f
    }

    suspend fun getRestingEnergy(): Float {
        return try {
            val user = currentUser ?: userDao.getFirst()
            if (user != null) {
                val weightKg = getUserWeightForDate(LocalDate.now().toString()) ?: HealthConstants.DEFAULT_WEIGHT_KG
                val heightCm = user.heightCm ?: HealthConstants.DEFAULT_HEIGHT_CM
                val isMale = user.gender == "male"
                val age = calculateAge(user.dateOfBirth)
                calculateBmr(weightKg, heightCm, isMale, age)
            } else {
                calculateBmr(
                    HealthConstants.DEFAULT_WEIGHT_KG,
                    HealthConstants.DEFAULT_HEIGHT_CM,
                    false,
                    HealthConstants.DEFAULT_AGE
                )
            }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
            calculateBmr(
                HealthConstants.DEFAULT_WEIGHT_KG,
                HealthConstants.DEFAULT_HEIGHT_CM,
                false,
                HealthConstants.DEFAULT_AGE
            )
        }
    }

    suspend fun getExerciseCalories(date: String = LocalDate.now().toString()): Float {
        val userId = requireUserId()
        return dailyHealthDao.getByDate(userId, date)?.caloriesBurned ?: 0f
    }

    private suspend fun getUserWeightForDate(date: String): Float? {
        val userId = requireUserId()
        val dailyHealth = dailyHealthDao.getByDate(userId, date)
        if (dailyHealth?.weightKg != null) return dailyHealth.weightKg
        val recentRecords = dailyHealthDao.getRecent(userId, HealthConstants.RECENT_DAYS_MONTH)
        return recentRecords.firstOrNull { it.weightKg != null }?.weightKg
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
            val birthDate = LocalDate.parse(dateOfBirth)
            ChronoUnit.YEARS.between(birthDate, LocalDate.now()).toInt()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (_: Exception) {
            HealthConstants.DEFAULT_AGE
        }
    }

    suspend fun updateWaterIntake(ml: Float, date: String = LocalDate.now().toString()) {
        require(ValidationUtils.isValidWaterIntake(ml)) { "Invalid water intake: $ml" }
        require(ValidationUtils.isValidDateString(date)) { "Invalid date: $date" }
        val userId = requireUserId()
        dailyHealthDao.insertOrUpdateWaterIntake(userId, date, ml)
    }

    suspend fun addWater(ml: Float = 250f, date: String = LocalDate.now().toString()): Float {
        require(ValidationUtils.isValidNonNegative(ml)) { "Invalid water amount: $ml" }
        require(ValidationUtils.isValidDateString(date)) { "Invalid date: $date" }
        val userId = requireUserId()
        return dailyHealthDao.addWaterAtomic(userId, date, ml)
    }

    private suspend fun syncDailyHealth(date: String) {
        try {
            val userId = requireUserId()
            val calories = mealDao.getDailyCalories(userId, date) ?: 0f
            val protein = mealDao.getDailyProtein(userId, date) ?: 0f
            val carbs = mealDao.getDailyCarbs(userId, date) ?: 0f
            val fat = mealDao.getDailyFat(userId, date) ?: 0f
            dailyHealthDao.insertOrUpdateNutrition(userId, date, calories, protein, carbs, fat)
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
            Log.e("NutritionRepository", "Failed to save meal record", e)
        }
    }

    suspend fun getCycleNutritionRecommendation(): CycleNutritionRecommendation? {
        try {
            val user = currentUser ?: userDao.getFirst() ?: return null
            if (user.gender == "male") return null

            val records = periodRecordDao.getByUserIdList(user.id).filter { !it.isDeleted }
            if (records.size < 2) return null

            val sorted = records.sortedByDescending { it.startDate }
            val lastRecord = sorted.first()
            val lastStart = LocalDate.parse(lastRecord.startDate)
            val prevRecord = sorted[1]
            val prevStart = LocalDate.parse(prevRecord.startDate)
            val cycleLength = ChronoUnit.DAYS.between(prevStart, lastStart).toInt()
                .coerceIn(HealthConstants.MIN_CYCLE_LENGTH, HealthConstants.MAX_CYCLE_LENGTH)

            val today = LocalDate.now()
            val phase = CyclePhase.fromDate(today, lastStart, cycleLength)
            val ovulationDay = (cycleLength - HealthConstants.DEFAULT_LUTEAL_PHASE_LENGTH)
                .coerceIn(HealthConstants.OVULATION_DAY_MIN, cycleLength - 1)

            val phaseDayRange = when (phase) {
                CyclePhase.MENSTRUAL -> "1-${HealthConstants.DEFAULT_PERIOD_LENGTH}"
                CyclePhase.FOLLICULAR -> "${HealthConstants.DEFAULT_PERIOD_LENGTH + 1}-${ovulationDay - 4}"
                CyclePhase.OVULATORY -> "${ovulationDay - 3}-${ovulationDay + 1}"
                CyclePhase.LUTEAL -> "${ovulationDay + 2}-$cycleLength"
            }

            val (focusNutrientsId, recommendedFoodsId, foodsToLimitId, generalAdviceId) = when (phase) {
                CyclePhase.MENSTRUAL -> Quadruple(
                    com.tianshang.health.core.common.R.string.nutrition_menstrual_nutrients,
                    com.tianshang.health.core.common.R.string.nutrition_menstrual_foods,
                    com.tianshang.health.core.common.R.string.nutrition_menstrual_limit,
                    com.tianshang.health.core.common.R.string.nutrition_menstrual_advice
                )
                CyclePhase.FOLLICULAR -> Quadruple(
                    com.tianshang.health.core.common.R.string.nutrition_follicular_nutrients,
                    com.tianshang.health.core.common.R.string.nutrition_follicular_foods,
                    com.tianshang.health.core.common.R.string.nutrition_follicular_limit,
                    com.tianshang.health.core.common.R.string.nutrition_follicular_advice
                )
                CyclePhase.OVULATORY -> Quadruple(
                    com.tianshang.health.core.common.R.string.nutrition_ovulatory_nutrients,
                    com.tianshang.health.core.common.R.string.nutrition_ovulatory_foods,
                    com.tianshang.health.core.common.R.string.nutrition_ovulatory_limit,
                    com.tianshang.health.core.common.R.string.nutrition_ovulatory_advice
                )
                CyclePhase.LUTEAL -> Quadruple(
                    com.tianshang.health.core.common.R.string.nutrition_luteal_nutrients,
                    com.tianshang.health.core.common.R.string.nutrition_luteal_foods,
                    com.tianshang.health.core.common.R.string.nutrition_luteal_limit,
                    com.tianshang.health.core.common.R.string.nutrition_luteal_advice
                )
            }

            return CycleNutritionRecommendation(
                currentPhase = phase,
                phaseDayRange = phaseDayRange,
                focusNutrientsResId = focusNutrientsId,
                recommendedFoodsResId = recommendedFoodsId,
                foodsToLimitResId = foodsToLimitId,
                generalAdviceResId = generalAdviceId
            )
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (_: Exception) {
            return null
        }
    }
}
