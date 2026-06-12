package com.tianshang.health.feature.period.di

import com.tianshang.health.core.period.api.PeriodPredictionEngine
import com.tianshang.health.feature.period.engine.PredictionEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PeriodApiModule {
    @Binds
    @Singleton
    abstract fun bindPredictionEngine(
        predictionEngine: PredictionEngine
    ): PeriodPredictionEngine
}
