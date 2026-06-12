package com.tianshang.health.feature.analysis.ml

import android.util.Log
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.database.entity.DailyHealth
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class MoodPredictorTest {

    private lateinit var tFLiteManager: TFLiteManager
    private lateinit var stringResolver: StringResolver
    private lateinit var predictor: MoodPredictor

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.w(any<String>(), any<String>(), any<Throwable>()) } returns 0
        tFLiteManager = mockk(relaxed = true)
        stringResolver = mockk()
        every { stringResolver.getString(any()) } answers { "[${it.invocation.args[0]}]" }
        every { stringResolver.getString(any(), *anyVararg()) } answers {
            val resId = it.invocation.args[0]
            val args = it.invocation.args.drop(1).joinToString(",")
            "[$resId:$args]"
        }
        predictor = MoodPredictor(tFLiteManager, stringResolver)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    private fun makeDaily(
        steps: Int? = 8000,
        exerciseMinutes: Int? = 30,
        sleepHours: Float? = 7.5f,
        deepSleepHours: Float? = 1.5f,
        sleepQuality: Int? = 3,
        caloriesIntake: Float? = 2000f,
        waterIntake: Float? = 1500f,
        heartRateResting: Int? = 72,
        moodScore: Int? = 3,
        stressLevel: Int? = 3
    ): DailyHealth {
        return DailyHealth(
            userId = 1L,
            date = "2026-06-10",
            steps = steps,
            caloriesBurned = null,
            exerciseMinutes = exerciseMinutes,
            sleepHours = sleepHours,
            deepSleepHours = deepSleepHours,
            sleepQuality = sleepQuality,
            caloriesIntake = caloriesIntake,
            waterIntake = waterIntake,
            heartRateResting = heartRateResting,
            moodScore = moodScore,
            stressLevel = stressLevel
        )
    }

    @Test
    fun `predict returns fallback when model not registered`() {
        val today = makeDaily()
        val result = predictor.predict(today, null)
        assert(result.isFallback)
        assert(result.moodScore == 3)
        assert(result.stressLevel == 3)
    }

    @Test
    fun `predict returns fallback when model not available`() {
        every { tFLiteManager.isModelAvailable(ModelRegistry.MOOD_MLP) } returns false

        val today = makeDaily()
        val result = predictor.predict(today, null)

        assert(result.isFallback)
    }

    @Test
    fun `predict returns valid mood and stress from inference`() {
        every { tFLiteManager.isModelAvailable(ModelRegistry.MOOD_MLP) } returns true
        every { tFLiteManager.runInference(ModelRegistry.MOOD_MLP, any(), any()) } returns
            floatArrayOf(0.5f, 0.3f)

        val today = makeDaily()
        val result = predictor.predict(today, null)

        verify { tFLiteManager.runInference(ModelRegistry.MOOD_MLP, any(), any()) }
        assert(!result.isFallback)
        assert(result.moodScore in 1..5)
        assert(result.stressLevel in 1..5)
        assert(result.confidence > 0f)
    }

    @Test
    fun `predict falls back when inference returns null`() {
        every { tFLiteManager.isModelAvailable(ModelRegistry.MOOD_MLP) } returns true
        every { tFLiteManager.runInference(ModelRegistry.MOOD_MLP, any(), any()) } returns null

        val today = makeDaily()
        val result = predictor.predict(today, null)

        assert(result.isFallback)
    }

    @Test
    fun `extractFeatures returns 15-element array`() {
        val today = makeDaily()
        val yesterday = makeDaily(steps = 5000, moodScore = 2, stressLevel = 4)

        val features = predictor.extractFeatures(today, yesterday, CyclePhase.LUTEAL, 14f)

        assert(features.size == 15)
        assert(features.all { it.isFinite() })
    }

    @Test
    fun `extractFeatures uses yesterday mood when available`() {
        val today = makeDaily(moodScore = 3, stressLevel = 3)
        val yesterday = makeDaily(moodScore = 5, stressLevel = 1)

        val features = predictor.extractFeatures(today, yesterday, CyclePhase.FOLLICULAR, 10f)

        assert(features[8] == (5 - 1) / 4f)
        assert(features[9] == (1 - 1) / 4f)
    }

    @Test
    fun `extractFeatures falls back to today mood without yesterday`() {
        val today = makeDaily(moodScore = 4, stressLevel = 2)

        val features = predictor.extractFeatures(today, null, CyclePhase.FOLLICULAR, 10f)

        assert(features[8] == (4 - 1) / 4f)
        assert(features[9] == (2 - 1) / 4f)
    }

    @Test
    fun `extractFeatures encodes cycle phase correctly`() {
        val today = makeDaily()

        val follicular = predictor.extractFeatures(today, null, CyclePhase.FOLLICULAR, 5f)
        assert(follicular[10] == 1f)
        assert(follicular[11] == 0f)
        assert(follicular[12] == 0f)

        val luteal = predictor.extractFeatures(today, null, CyclePhase.LUTEAL, 20f)
        assert(luteal[10] == 0f)
        assert(luteal[11] == 1f)
        assert(luteal[12] == 0f)

        val menstrual = predictor.extractFeatures(today, null, CyclePhase.MENSTRUAL, 2f)
        assert(menstrual[10] == 0f)
        assert(menstrual[11] == 0f)
        assert(menstrual[12] == 1f)
    }

    @Test
    fun `extractFeatures handles null fields with defaults`() {
        val today = DailyHealth(userId = 1L, date = "2026-06-10")

        val features = predictor.extractFeatures(today, null, CyclePhase.FOLLICULAR, 1f)

        assert(features.size == 15)
        assert(features.all { it.isFinite() })
        assert(features[0] == 8000 / 10000f)
        assert(features[2] == 7.5f / 12f)
    }

    @Test
    fun `predict handles exception gracefully`() {
        every { tFLiteManager.isModelAvailable(ModelRegistry.MOOD_MLP) } returns true
        every { tFLiteManager.runInference(ModelRegistry.MOOD_MLP, any(), any()) } throws
            RuntimeException("Failed")

        val today = makeDaily()
        val result = predictor.predict(today, null)

        assert(result.isFallback)
        assert(result.moodScore == 3)
    }

    @Test
    fun `predict clamps output to 1-5 range`() {
        every { tFLiteManager.isModelAvailable(ModelRegistry.MOOD_MLP) } returns true
        every { tFLiteManager.runInference(ModelRegistry.MOOD_MLP, any(), any()) } returns
            floatArrayOf(10f, -10f)

        val today = makeDaily()
        val result = predictor.predict(today, null)

        assert(result.moodScore == 5)
        assert(result.stressLevel == 1)
    }
}
