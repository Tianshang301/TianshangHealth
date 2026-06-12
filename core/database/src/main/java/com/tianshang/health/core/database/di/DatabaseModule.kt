package com.tianshang.health.core.database.di

import android.content.Context
import androidx.room.Room
import com.tianshang.health.core.database.TianshangDatabase
import com.tianshang.health.core.database.dao.DailyHealthDao
import com.tianshang.health.core.database.dao.DailySymptomDao
import com.tianshang.health.core.database.dao.MealDao
import com.tianshang.health.core.database.dao.PeriodRecordDao
import com.tianshang.health.core.database.dao.PredictionLogDao
import com.tianshang.health.core.database.dao.StepsDao
import com.tianshang.health.core.database.dao.UserDao
import com.tianshang.health.core.database.dao.WorkoutDao
import com.tianshang.health.core.security.encryption.SqlCipherManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TianshangDatabase {
        val factory = SqlCipherManager.getSupportFactory(context)

        return Room.databaseBuilder(
            context,
            TianshangDatabase::class.java,
            TianshangDatabase.DATABASE_NAME
        )
            .openHelperFactory(factory)
            .addMigrations(
                TianshangDatabase.MIGRATION_2_3,
                TianshangDatabase.MIGRATION_3_4,
                TianshangDatabase.MIGRATION_4_5,
                TianshangDatabase.MIGRATION_5_6,
                TianshangDatabase.MIGRATION_6_7,
                TianshangDatabase.MIGRATION_7_8,
                TianshangDatabase.MIGRATION_8_9,
                TianshangDatabase.MIGRATION_9_10
            )
            .build()
    }

    @Provides
    fun provideUserDao(database: TianshangDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun providePeriodRecordDao(database: TianshangDatabase): PeriodRecordDao {
        return database.periodRecordDao()
    }

    @Provides
    fun provideDailySymptomDao(database: TianshangDatabase): DailySymptomDao {
        return database.dailySymptomDao()
    }

    @Provides
    fun provideDailyHealthDao(database: TianshangDatabase): DailyHealthDao {
        return database.dailyHealthDao()
    }

    @Provides
    fun provideStepsDao(database: TianshangDatabase): StepsDao {
        return database.stepsDao()
    }

    @Provides
    fun provideWorkoutDao(database: TianshangDatabase): WorkoutDao {
        return database.workoutDao()
    }

    @Provides
    fun provideMealDao(database: TianshangDatabase): MealDao {
        return database.mealDao()
    }

    @Provides
    fun providePredictionLogDao(database: TianshangDatabase): PredictionLogDao {
        return database.predictionLogDao()
    }
}
