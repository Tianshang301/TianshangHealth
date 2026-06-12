package com.tianshang.health.feature.fitness.data.repository

import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.common.util.ValidationUtils
import com.tianshang.health.core.database.dao.DailyHealthDao
import com.tianshang.health.core.database.dao.StepsDao
import com.tianshang.health.core.database.dao.UserDao
import com.tianshang.health.core.database.dao.WorkoutDao
import com.tianshang.health.core.database.entity.DailyHealth
import com.tianshang.health.core.database.entity.User
import com.tianshang.health.core.database.entity.WorkoutRecord
import com.tianshang.health.feature.fitness.util.CalorieCalculator
import com.tianshang.health.feature.fitness.util.ExerciseType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FitnessRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val userDao: UserDao,
    private val dailyHealthDao: DailyHealthDao,
    private val stepsDao: StepsDao
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

    suspend fun autoCalculateCalories(
        exerciseTypeValue: String,
        durationMinutes: Int,
        date: String
    ): Float {
        require(ValidationUtils.isValidExerciseType(exerciseTypeValue)) { "Invalid exerciseType: $exerciseTypeValue" }
        require(ValidationUtils.isValidExerciseMinutes(durationMinutes)) { "Invalid durationMinutes: $durationMinutes" }
        require(ValidationUtils.isValidDateString(date)) { "Invalid date: $date" }
        val exerciseType = ExerciseType.fromValue(exerciseTypeValue)
        val weightKg = getUserWeightForDate(date)
        val heightCm = getUserHeight()
        val isMale = getUserGender() == "male"
        return CalorieCalculator.calculateKcal(exerciseType.metValue, weightKg, heightCm, isMale, durationMinutes)
    }

    private suspend fun getUserWeightForDate(date: String): Float? {
        val userId = requireUserId()
        val dailyHealth = dailyHealthDao.getByDate(userId, date)
        if (dailyHealth?.weightKg != null) return dailyHealth.weightKg
        val recentRecords = dailyHealthDao.getRecent(userId, 30)
        return recentRecords.firstOrNull { it.weightKg != null }?.weightKg
    }

    suspend fun addWorkout(
        exerciseType: String,
        durationMinutes: Int,
        caloriesBurned: Float? = null,
        distanceMeters: Float? = null,
        averageHeartRate: Int? = null,
        intensity: String? = null,
        notes: String? = null,
        date: String = LocalDate.now().toString()
    ): Long {
        require(ValidationUtils.isValidExerciseType(exerciseType)) { "Invalid exerciseType: $exerciseType" }
        require(ValidationUtils.isValidExerciseMinutes(durationMinutes)) { "Invalid durationMinutes: $durationMinutes" }
        require(ValidationUtils.isValidCalories(caloriesBurned)) { "Invalid caloriesBurned: $caloriesBurned" }
        require(ValidationUtils.isValidHeartRate(averageHeartRate)) { "Invalid averageHeartRate: $averageHeartRate" }
        require(ValidationUtils.isValidDateString(date)) { "Invalid date: $date" }
        val userId = requireUserId()
        return workoutDao.insert(
            WorkoutRecord(
                userId = userId,
                date = date,
                exerciseType = exerciseType,
                durationMinutes = durationMinutes,
                caloriesBurned = caloriesBurned,
                distanceMeters = distanceMeters,
                averageHeartRate = averageHeartRate,
                intensity = intensity,
                notes = notes
            )
        )
    }

    suspend fun updateWorkout(record: WorkoutRecord) {
        require(ValidationUtils.isValidId(record.id)) { "Invalid workout id: ${record.id}" }
        require(
            ValidationUtils.isValidExerciseType(record.exerciseType)
        ) { "Invalid exerciseType: ${record.exerciseType}" }
        require(
            ValidationUtils.isValidExerciseMinutes(record.durationMinutes)
        ) { "Invalid durationMinutes: ${record.durationMinutes}" }
        workoutDao.update(record)
    }

    suspend fun deleteWorkout(record: WorkoutRecord) {
        require(ValidationUtils.isValidId(record.id)) { "Invalid workout id: ${record.id}" }
        workoutDao.delete(record)
    }

    fun getWorkoutsByDate(date: String): Flow<List<WorkoutRecord>> {
        val userId = currentUser?.id ?: return kotlinx.coroutines.flow.emptyFlow()
        return workoutDao.getByDate(userId, date)
    }

    fun getAllWorkouts(): Flow<List<WorkoutRecord>> {
        val userId = currentUser?.id ?: return kotlinx.coroutines.flow.emptyFlow()
        return workoutDao.getAllByUser(userId)
    }

    fun getWorkoutsByDateRange(startDate: String, endDate: String): Flow<List<WorkoutRecord>> {
        val userId = currentUser?.id ?: return kotlinx.coroutines.flow.emptyFlow()
        return workoutDao.getByDateRangeFlow(userId, startDate, endDate)
    }

    suspend fun getTotalDurationThisWeek(): Int {
        val userId = requireUserId()
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.value - 1L)
        return workoutDao.getTotalDuration(userId, startOfWeek.toString(), today.toString()) ?: 0
    }

    suspend fun getTotalCaloriesThisWeek(): Float {
        val userId = requireUserId()
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.value - 1L)
        return workoutDao.getTotalCalories(userId, startOfWeek.toString(), today.toString()) ?: 0f
    }

    suspend fun getTotalCaloriesByDate(date: String): Float {
        val userId = requireUserId()
        return workoutDao.getDailyCalories(userId, date) ?: 0f
    }

    suspend fun getTodaySteps(): Int {
        val userId = requireUserId()
        val today = LocalDate.now().toString()
        return stepsDao.getByDate(userId, today)?.count ?: 0
    }

    suspend fun getTodayGoal(): Int {
        val userId = requireUserId()
        val today = LocalDate.now().toString()
        return stepsDao.getByDate(userId, today)?.goal ?: HealthConstants.DEFAULT_STEPS_GOAL
    }

    suspend fun getTodayStepsCalories(): Float {
        val steps = getTodaySteps()
        val weightKg = getUserWeightForDate(LocalDate.now().toString())
        val heightCm = getUserHeight()
        return CalorieCalculator.stepsToKcal(steps, weightKg, heightCm)
    }

    suspend fun getCombinedDailyCalories(date: String): Float {
        val workoutCalories = getTotalCaloriesByDate(date)
        val steps = if (date == LocalDate.now().toString()) {
            getTodaySteps()
        } else {
            val userId = requireUserId()
            stepsDao.getByDate(userId, date)?.count ?: 0
        }
        val weightKg = getUserWeightForDate(date)
        val heightCm = getUserHeight()
        val stepsCalories = CalorieCalculator.stepsToKcal(steps, weightKg, heightCm)
        return workoutCalories + stepsCalories
    }

    suspend fun getWeeklySteps(): List<com.tianshang.health.core.database.entity.DailySteps> {
        val userId = requireUserId()
        val today = LocalDate.now()
        val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        return stepsDao.getByDateRange(userId, weekStart.toString(), today.toString())
    }

    suspend fun getTotalStepsThisWeek(): Int {
        val userId = requireUserId()
        val today = LocalDate.now()
        val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        return stepsDao.getTotalSteps(userId, weekStart.toString(), today.toString()) ?: 0
    }

    suspend fun getTotalStepsAllTime(): Int {
        val userId = requireUserId()
        return stepsDao.getTotalStepsAllTime(userId) ?: 0
    }

    suspend fun updateStepsGoal(goal: Int) {
        require(ValidationUtils.isValidNonNegativeInt(goal)) { "Invalid goal: $goal" }
        val userId = requireUserId()
        val today = LocalDate.now().toString()
        val existing = stepsDao.getByDate(userId, today)
        if (existing != null) {
            stepsDao.update(existing.copy(goal = goal, updatedAt = System.currentTimeMillis()))
        } else {
            stepsDao.insert(
                com.tianshang.health.core.database.entity.DailySteps(
                    userId = userId,
                    date = today,
                    count = 0,
                    goal = goal
                )
            )
        }
    }

    suspend fun saveWeight(weightKg: Float, date: String) {
        require(ValidationUtils.isValidWeight(weightKg)) { "Invalid weightKg: $weightKg" }
        require(ValidationUtils.isValidDateString(date)) { "Invalid date: $date" }
        val userId = requireUserId()
        val existing = dailyHealthDao.getByDate(userId, date)
        if (existing != null) {
            dailyHealthDao.update(existing.copy(weightKg = weightKg))
        } else {
            dailyHealthDao.insert(
                DailyHealth(userId = userId, date = date, weightKg = weightKg)
            )
        }
    }

    suspend fun getLatestWeight(): Float? {
        val userId = requireUserId()
        return dailyHealthDao.getRecent(userId, 30).firstOrNull { it.weightKg != null }?.weightKg
    }

    suspend fun getUserHeight(): Float? {
        val user = currentUser ?: userDao.getFirst()
        currentUser = user
        return user?.heightCm
    }

    suspend fun getUserGender(): String? {
        val user = currentUser ?: userDao.getFirst()
        currentUser = user
        return user?.gender
    }

    suspend fun updateHeight(heightCm: Float) {
        require(ValidationUtils.isValidHeight(heightCm)) { "Invalid heightCm: $heightCm" }
        val userId = requireUserId()
        userDao.updateHeight(userId, heightCm)
    }

    suspend fun getWorkoutCountThisWeek(): Int {
        val userId = requireUserId()
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.value - 1L)
        return workoutDao.getCount(userId, startOfWeek.toString(), today.toString())
    }

    suspend fun getDistinctExerciseTypes(): List<String> {
        val userId = requireUserId()
        return workoutDao.getDistinctExerciseTypes(userId)
    }
}
