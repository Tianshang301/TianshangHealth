package com.tianshang.health.feature.analysis.domain

import com.tianshang.health.core.common.R
import com.tianshang.health.core.database.entity.DailyHealth
import org.junit.Test

class AnalyticsEngineTest {

    private val testRecords = listOf(
        DailyHealth(
            userId = 1, date = "2026-06-01",
            steps = 8000, caloriesBurned = 300f, exerciseMinutes = 30, exerciseType = "跑步",
            sleepHours = 7.5f, deepSleepHours = 2.0f, sleepQuality = 4,
            caloriesIntake = 1800f, waterIntake = 1500f, proteinGrams = 60f, carbsGrams = 200f, fatGrams = 50f,
            moodScore = 4, stressLevel = 2
        ),
        DailyHealth(
            userId = 1, date = "2026-06-02",
            steps = 5000, caloriesBurned = 150f, exerciseMinutes = 20, exerciseType = "瑜伽",
            sleepHours = 6.0f, deepSleepHours = 1.5f, sleepQuality = 3,
            caloriesIntake = 1600f, waterIntake = 1200f, proteinGrams = 50f, carbsGrams = 180f, fatGrams = 40f,
            moodScore = 3, stressLevel = 3
        ),
        DailyHealth(
            userId = 1, date = "2026-06-03",
            steps = 10000, caloriesBurned = 400f, exerciseMinutes = 45, exerciseType = "跑步",
            sleepHours = 8.0f, deepSleepHours = 2.5f, sleepQuality = 5,
            caloriesIntake = 2000f, waterIntake = 2000f, proteinGrams = 70f, carbsGrams = 220f, fatGrams = 55f,
            moodScore = 5, stressLevel = 1
        )
    )

    @Test
    fun `computeNutrition returns correct averages`() {
        val result = AnalyticsEngine.computeNutrition(testRecords)

        assert(result.avgCalories == (1800f + 1600f + 2000f) / 3f) {
            "Expected avgCalories ${(1800f + 1600f + 2000f) / 3f}, got ${result.avgCalories}"
        }
        assert(result.avgProteinGrams == (60f + 50f + 70f) / 3f)
        assert(result.avgCarbsGrams == (200f + 180f + 220f) / 3f)
        assert(result.avgFatGrams == (50f + 40f + 55f) / 3f)
        assert(result.avgWaterMl == (1500f + 1200f + 2000f) / 3f)
    }

    @Test
    fun `computeNutrition returns zero for empty records`() {
        val result = AnalyticsEngine.computeNutrition(emptyList())

        assert(result.avgCalories == 0f)
        assert(result.avgProteinGrams == 0f)
        assert(result.avgCarbsGrams == 0f)
        assert(result.avgFatGrams == 0f)
        assert(result.avgWaterMl == 0f)
        assert(result.dailyCalories.isEmpty())
    }

    @Test
    fun `computeNutrition handles partial null fields`() {
        val partial = listOf(
            DailyHealth(userId = 1, date = "2026-06-01", caloriesIntake = 1500f),
            DailyHealth(userId = 1, date = "2026-06-02")
        )
        val result = AnalyticsEngine.computeNutrition(partial)

        // Averages only over non-null records (1 record with caloriesIntake)
        assert(result.avgCalories == 1500f) {
            "Expected avgCalories 1500f, got ${result.avgCalories}"
        }
        assert(result.avgProteinGrams == 0f)
    }

    @Test
    fun `computeSleep returns correct averages`() {
        val result = AnalyticsEngine.computeSleep(testRecords)

        assert(result.avgHours == (7.5f + 6.0f + 8.0f) / 3f)
        assert(result.avgDeepHours == (2.0f + 1.5f + 2.5f) / 3f)
        assert(result.avgQuality == (4f + 3f + 5f) / 3f)
    }

    @Test
    fun `computeSleep returns zero for empty records`() {
        val result = AnalyticsEngine.computeSleep(emptyList())

        assert(result.avgHours == 0f)
        assert(result.avgDeepHours == 0f)
        assert(result.avgQuality == 0f)
    }

    @Test
    fun `computeSleep handles null sleepHours`() {
        val records = listOf(
            DailyHealth(userId = 1, date = "2026-06-01", sleepHours = 7f),
            DailyHealth(userId = 1, date = "2026-06-02")
        )
        val result = AnalyticsEngine.computeSleep(records)

        // Averages only over non-null records (1 record with sleepHours)
        assert(result.avgHours == 7f)
        assert(result.avgDeepHours == 0f)
    }

    @Test
    fun `computeExercise returns correct totals`() {
        val result = AnalyticsEngine.computeExercise(testRecords)

        assert(result.totalMinutes == 30 + 20 + 45)
        assert(result.totalCaloriesBurned == 300f + 150f + 400f)
        assert(result.typeDistribution.any { it.type == "跑步" })
        assert(result.typeDistribution.any { it.type == "瑜伽" })
    }

    @Test
    fun `computeExercise returns zero for empty records`() {
        val result = AnalyticsEngine.computeExercise(emptyList())

        assert(result.totalMinutes == 0)
        assert(result.totalCaloriesBurned == 0f)
        assert(result.typeDistribution.isEmpty())
    }

    @Test
    fun `computeExercise handles no exercise records`() {
        val records = listOf(
            DailyHealth(userId = 1, date = "2026-06-01", exerciseMinutes = 0),
            DailyHealth(userId = 1, date = "2026-06-02")
        )
        val result = AnalyticsEngine.computeExercise(records)

        assert(result.totalMinutes == 0)
        assert(result.avgMinutesPerDay == 0f)
    }

    @Test
    fun `computeCalorieBalance returns correct averages`() {
        val result = AnalyticsEngine.computeCalorieBalance(testRecords)

        val expectedAvgIn = (1800f + 1600f + 2000f) / 3f
        val expectedAvgOut = (300f + 150f + 400f) / 3f

        assert(result.avgCaloriesIn == expectedAvgIn)
        assert(result.avgCaloriesBurned == expectedAvgOut)
        assert(result.netDaily.size == 3)
    }

    @Test
    fun `computeCalorieBalance returns zero for empty records`() {
        val result = AnalyticsEngine.computeCalorieBalance(emptyList())

        assert(result.avgCaloriesIn == 0f)
        assert(result.avgCaloriesBurned == 0f)
        assert(result.netDaily.isEmpty())
    }

    @Test
    fun `computePhaseComparisons returns follicular and luteal data`() {
        val follicularRange = Pair("2026-06-01", "2026-06-02")
        val lutealRange = Pair("2026-06-03", "2026-06-03")

        val result = AnalyticsEngine.computePhaseComparisons(testRecords, follicularRange, lutealRange)

        assert(result.size == 2)
        val follicular = result.find { it.phaseNameResId == R.string.phase_follicular }
        val luteal = result.find { it.phaseNameResId == R.string.phase_luteal }

        assert(follicular != null)
        assert(luteal != null)
        assert(follicular!!.sleepAvg > 0)
        assert(luteal!!.sleepAvg > 0)
    }

    @Test
    fun `computePhaseComparisons returns empty when no ranges given`() {
        val result = AnalyticsEngine.computePhaseComparisons(testRecords, null, null)

        assert(result.isEmpty())
    }

    @Test
    fun `computePhaseComparisons returns single phase when only one range given`() {
        val follicularRange = Pair("2026-06-01", "2026-06-03")

        val result = AnalyticsEngine.computePhaseComparisons(testRecords, follicularRange, null)

        assert(result.size == 1)
        assert(result[0].phaseNameResId == R.string.phase_follicular)
    }

    @Test
    fun `computePhaseComparisons handles empty records`() {
        val follicularRange = Pair("2026-06-01", "2026-06-03")
        val result = AnalyticsEngine.computePhaseComparisons(emptyList(), follicularRange, null)

        assert(result.size == 1)
        assert(result[0].sleepAvg == 0f)
    }

    @Test
    fun `computeNutrition daily data includes all records`() {
        val result = AnalyticsEngine.computeNutrition(testRecords)

        assert(result.dailyCalories.size == 3)
        assert(result.dailyProtein.size == 3)
        assert(result.dailyCarbs.size == 3)
        assert(result.dailyFat.size == 3)
    }

    @Test
    fun `computeSleep daily data includes all records`() {
        val result = AnalyticsEngine.computeSleep(testRecords)

        assert(result.dailyHours.size == 3)
        assert(result.dailyDeepHours.size == 3)
        assert(result.dailyQuality.size == 3)
    }

    @Test
    fun `computeExercise returns per-day data`() {
        val result = AnalyticsEngine.computeExercise(testRecords)

        assert(result.dailyMinutes.size == 3)
    }

    @Test
    fun `computeCalorieBalance net daily includes all records`() {
        val result = AnalyticsEngine.computeCalorieBalance(testRecords)

        assert(result.netDaily.size == 3)
        // net = caloriesIntake - (restingEnergy + caloriesBurned)
        // restingEnergy for null user (60kg female, 170cm, 30yr): 10*60 + 6.25*170 - 5*30 - 161 = 1351.5
        val restingEnergy = 1351.5f
        val firstNet = result.netDaily[0]
        assert(firstNet.second == 1800f - (restingEnergy + 300f))
    }
}
