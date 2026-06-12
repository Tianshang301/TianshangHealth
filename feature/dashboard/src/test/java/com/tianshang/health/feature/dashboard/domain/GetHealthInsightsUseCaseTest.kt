package com.tianshang.health.feature.dashboard.domain

import com.tianshang.health.core.database.dao.DailyHealthDao
import com.tianshang.health.core.database.dao.DailySymptomDao
import com.tianshang.health.core.database.dao.PeriodRecordDao
import com.tianshang.health.core.database.entity.DailyHealth
import com.tianshang.health.core.database.entity.PeriodRecord
import com.tianshang.health.core.database.entity.User
import com.tianshang.health.core.database.repository.UserRepository
import com.tianshang.health.feature.onboarding.model.Gender
import com.tianshang.health.feature.steps.domain.GetCycleStepsInsightUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetHealthInsightsUseCaseTest {

    private val userRepository: UserRepository = mockk()
    private val periodRecordDao: PeriodRecordDao = mockk()
    private val dailyHealthDao: DailyHealthDao = mockk()
    private val dailySymptomDao: DailySymptomDao = mockk()
    private val getCycleStepsInsight: GetCycleStepsInsightUseCase = mockk()
    private lateinit var useCase: GetHealthInsightsUseCase

    private val testUser = User(id = 1, name = "Test", gender = "female")
    private val today = java.time.LocalDate.now()
    private val todayStr = today.toString()

    @Before
    fun setUp() {
        coEvery { userRepository.getOrCreateDefault() } returns testUser
        useCase = GetHealthInsightsUseCase(
            userRepository, periodRecordDao, dailyHealthDao,
            dailySymptomDao, getCycleStepsInsight
        )
    }

    @Test
    fun `invoke returns empty result when no records`() = runTest {
        coEvery { periodRecordDao.getByUserIdList(1) } returns emptyList()
        coEvery { dailyHealthDao.getTodayData(1, todayStr) } returns null

        val result = useCase(Gender.FEMALE)

        assert(!result.hasData)
        assert(result.data.isEmpty())
    }

    @Test
    fun `invoke returns insights when period records exist`() = runTest {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = todayStr, endDate = today.plusDays(3).toString())
        )
        coEvery { periodRecordDao.getByUserIdList(1) } returns records
        coEvery { dailyHealthDao.getByDateRange(any(), any(), any()) } returns emptyList()
        coEvery { dailyHealthDao.getTodayData(1, todayStr) } returns null
        coEvery { getCycleStepsInsight() } returns null

        val result = useCase(Gender.FEMALE)

        // phaseStats is non-null with records, so hasData becomes true even without insights
        assert(result.hasData)
        assert(result.data.isEmpty())
    }

    @Test
    fun `invoke adds steps insight when phase data exists`() = runTest {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-05-01", endDate = "2026-05-05"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-05-29", endDate = "2026-06-02")
        )
        coEvery { periodRecordDao.getByUserIdList(1) } returns records
        coEvery { dailyHealthDao.getByDateRange(any(), any(), any()) } returns listOf(
            DailyHealth(userId = 1, date = "2026-05-15", steps = 8000),
            DailyHealth(userId = 1, date = "2026-06-01", steps = 5000)
        )
        coEvery { dailyHealthDao.getTodayData(1, todayStr) } returns null
        coEvery { getCycleStepsInsight() } returns null

        val result = useCase(Gender.FEMALE)

        assert(result.hasData)
        assert(result.data.any { it is InsightData.StepsActivity })
    }

    @Test
    fun `invoke adds mood insight when data exists`() = runTest {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-05-01", endDate = "2026-05-05"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-05-29", endDate = "2026-06-02")
        )
        coEvery { periodRecordDao.getByUserIdList(1) } returns records
        coEvery { dailyHealthDao.getByDateRange(any(), any(), any()) } returns listOf(
            DailyHealth(userId = 1, date = "2026-05-15", moodScore = 4),
            DailyHealth(userId = 1, date = "2026-06-01", moodScore = 3)
        )
        coEvery { dailyHealthDao.getTodayData(1, todayStr) } returns null
        coEvery { getCycleStepsInsight() } returns null

        val result = useCase(Gender.FEMALE)

        assert(result.hasData)
        assert(result.data.any { it is InsightData.MoodChange })
    }

    @Test
    fun `invoke adds sleep insight when data exists`() = runTest {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-05-01", endDate = "2026-05-05"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-05-29", endDate = "2026-06-02")
        )
        coEvery { periodRecordDao.getByUserIdList(1) } returns records
        coEvery { dailyHealthDao.getByDateRange(any(), any(), any()) } returns listOf(
            DailyHealth(userId = 1, date = "2026-05-15", sleepQuality = 4),
            DailyHealth(userId = 1, date = "2026-06-01", sleepQuality = 3)
        )
        coEvery { dailyHealthDao.getTodayData(1, todayStr) } returns null
        coEvery { getCycleStepsInsight() } returns null

        val result = useCase(Gender.FEMALE)

        assert(result.hasData)
        assert(result.data.any { it is InsightData.SleepChange })
    }

    @Test
    fun `invoke adds today tips when mood is low`() = runTest {
        coEvery { periodRecordDao.getByUserIdList(1) } returns emptyList()
        coEvery { dailyHealthDao.getTodayData(1, todayStr) } returns
            DailyHealth(userId = 1, date = todayStr, moodScore = 1, stressLevel = 4, sleepQuality = 2)

        val result = useCase(Gender.FEMALE)

        assert(result.hasData)
        assert(result.data.any { it is InsightData.TodayMood })
        assert(result.data.any { it is InsightData.TodayStress })
        assert(result.data.any { it is InsightData.TodaySleep })
    }

    @Test
    fun `invoke adds cycle steps insight for female users`() = runTest {
        val records = listOf(
            PeriodRecord(id = 1, userId = 1, startDate = "2026-05-01"),
            PeriodRecord(id = 2, userId = 1, startDate = "2026-05-29")
        )
        coEvery { periodRecordDao.getByUserIdList(1) } returns records
        coEvery { dailyHealthDao.getByDateRange(any(), any(), any()) } returns emptyList()
        coEvery { dailyHealthDao.getTodayData(1, todayStr) } returns null
        coEvery { getCycleStepsInsight() } returns io.mockk.mockk {
            coEvery { follicularAvg } returns 8000f
            coEvery { lutealAvg } returns 5000f
        }

        val result = useCase(Gender.FEMALE)

        assert(result.data.any { it is InsightData.CycleStepsResult })
    }

    @Test
    fun `invoke skips cycle steps insight for male users`() = runTest {
        coEvery { periodRecordDao.getByUserIdList(1) } returns emptyList()
        coEvery { dailyHealthDao.getTodayData(1, todayStr) } returns null

        val result = useCase(Gender.MALE)

        assert(!result.data.any { it is InsightData.CycleStepsResult })
    }

    @Test
    fun `invoke still returns insights for male users with health data`() = runTest {
        coEvery { periodRecordDao.getByUserIdList(1) } returns emptyList()
        coEvery { dailyHealthDao.getTodayData(1, todayStr) } returns
            DailyHealth(userId = 1, date = todayStr, steps = 5000, moodScore = 2)

        val result = useCase(Gender.MALE)

        assert(result.hasData)
        assert(result.data.any { it is InsightData.TodayMood })
    }
}
