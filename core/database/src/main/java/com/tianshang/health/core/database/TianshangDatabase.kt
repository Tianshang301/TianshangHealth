package com.tianshang.health.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tianshang.health.core.database.dao.DailyHealthDao
import com.tianshang.health.core.database.dao.DailySymptomDao
import com.tianshang.health.core.database.dao.MealDao
import com.tianshang.health.core.database.dao.PeriodRecordDao
import com.tianshang.health.core.database.dao.PredictionLogDao
import com.tianshang.health.core.database.dao.StepsDao
import com.tianshang.health.core.database.dao.UserDao
import com.tianshang.health.core.database.dao.WorkoutDao
import com.tianshang.health.core.database.entity.DailyHealth
import com.tianshang.health.core.database.entity.DailySteps
import com.tianshang.health.core.database.entity.DailySymptom
import com.tianshang.health.core.database.entity.MealRecord
import com.tianshang.health.core.database.entity.PeriodRecord
import com.tianshang.health.core.database.entity.PredictionLog
import com.tianshang.health.core.database.entity.User
import com.tianshang.health.core.database.entity.WorkoutRecord

@Database(
    entities = [
        User::class,
        PeriodRecord::class,
        DailySymptom::class,
        DailyHealth::class,
        DailySteps::class,
        WorkoutRecord::class,
        MealRecord::class,
        PredictionLog::class
    ],
    version = 1,
    exportSchema = true
)
abstract class TianshangDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun periodRecordDao(): PeriodRecordDao
    abstract fun dailySymptomDao(): DailySymptomDao
    abstract fun dailyHealthDao(): DailyHealthDao
    abstract fun stepsDao(): StepsDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun mealDao(): MealDao
    abstract fun predictionLogDao(): PredictionLogDao

    companion object {
        const val DATABASE_NAME = "tianshang_health.db"
    }
}
