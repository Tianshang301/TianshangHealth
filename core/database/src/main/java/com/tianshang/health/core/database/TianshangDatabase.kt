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
    version = 10,
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

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN gender TEXT NOT NULL DEFAULT 'female'")
            }
        }

        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add soft delete fields to period_records
                database.execSQL("ALTER TABLE period_records ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE period_records ADD COLUMN deletedAt INTEGER")
            }
        }

        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add height_cm field to users for BMI calculation
                database.execSQL("ALTER TABLE users ADD COLUMN heightCm REAL")
            }
        }

        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS workout_records (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        userId INTEGER NOT NULL,
                        date TEXT NOT NULL,
                        exerciseType TEXT NOT NULL,
                        durationMinutes INTEGER NOT NULL,
                        caloriesBurned REAL,
                        distanceMeters REAL,
                        averageHeartRate INTEGER,
                        intensity TEXT,
                        notes TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                    )"""
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_records_userId ON workout_records(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_records_date ON workout_records(date)")
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_workout_records_userId_date ON workout_records(userId, date)"
                )
            }
        }

        val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS meal_records (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        userId INTEGER NOT NULL,
                        date TEXT NOT NULL,
                        mealType TEXT NOT NULL,
                        foodName TEXT NOT NULL,
                        calories REAL,
                        proteinGrams REAL,
                        carbsGrams REAL,
                        fatGrams REAL,
                        servingSize TEXT,
                        notes TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                    )"""
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_meal_records_userId ON meal_records(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_meal_records_date ON meal_records(date)")
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_meal_records_userId_date ON meal_records(userId, date)"
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_meal_records_mealType ON meal_records(mealType)")
            }
        }

        val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN dateOfBirth TEXT")
            }
        }

        val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS prediction_logs (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        userId INTEGER NOT NULL,
                        predictedStartDate TEXT NOT NULL,
                        predictedEndDate TEXT,
                        actualStartDate TEXT,
                        errorDays INTEGER,
                        confidence TEXT NOT NULL,
                        algorithmVersion TEXT NOT NULL DEFAULT 'v2.0',
                        decayFactorUsed REAL NOT NULL DEFAULT 0.75,
                        createdAt INTEGER NOT NULL,
                        resolvedAt INTEGER,
                        tflitePredictedStartDate TEXT,
                        rulesPredictedStartDate TEXT,
                        tfliteModelUsed TEXT,
                        tfliteConfidence REAL,
                        agreementScore REAL,
                        FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                    )"""
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_prediction_logs_userId ON prediction_logs(userId)")
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_prediction_logs_createdAt ON prediction_logs(createdAt)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_prediction_logs_tfliteModelUsed ON prediction_logs(tfliteModelUsed)"
                )
            }
        }

        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("DROP INDEX IF EXISTS index_workout_records_userId_date")
                database.execSQL("DROP INDEX IF EXISTS index_workout_records_userId")
                database.execSQL("DROP INDEX IF EXISTS index_workout_records_date")
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS workout_records_new (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        userId INTEGER NOT NULL,
                        date TEXT NOT NULL,
                        exerciseType TEXT NOT NULL,
                        durationMinutes INTEGER NOT NULL,
                        caloriesBurned REAL,
                        distanceMeters REAL,
                        averageHeartRate INTEGER,
                        intensity TEXT,
                        notes TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                    )"""
                )
                database.execSQL("INSERT INTO workout_records_new SELECT * FROM workout_records")
                database.execSQL("DROP TABLE workout_records")
                database.execSQL("ALTER TABLE workout_records_new RENAME TO workout_records")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_records_userId ON workout_records(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_records_date ON workout_records(date)")
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_workout_records_userId_date ON workout_records(userId, date)"
                )
            }
        }
    }
}
