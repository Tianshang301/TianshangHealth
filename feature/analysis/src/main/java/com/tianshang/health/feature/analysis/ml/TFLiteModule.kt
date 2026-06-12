package com.tianshang.health.feature.analysis.ml

import android.content.Context
import com.tianshang.health.core.common.util.StringResolver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TFLiteModule {

    @Provides
    @Singleton
    fun provideTFLiteManager(
        @ApplicationContext context: Context
    ): TFLiteManager {
        return TFLiteManager(context)
    }

    @Provides
    @Singleton
    fun provideFeatureExtractor(): FeatureExtractor {
        return FeatureExtractor()
    }

    @Provides
    @Singleton
    fun providePredictionEnhancer(
        tFLiteManager: TFLiteManager,
        featureExtractor: FeatureExtractor,
        stringResolver: StringResolver
    ): PredictionEnhancer {
        return PredictionEnhancer(tFLiteManager, featureExtractor, stringResolver)
    }

    @Provides
    @Singleton
    fun provideMoodPredictor(
        tFLiteManager: TFLiteManager,
        stringResolver: StringResolver
    ): MoodPredictor {
        return MoodPredictor(tFLiteManager, stringResolver)
    }
}
