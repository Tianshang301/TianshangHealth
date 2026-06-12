package com.tianshang.health.feature.period.viewmodel

import com.tianshang.health.core.database.dao.DailyHealthDao
import com.tianshang.health.core.database.entity.DailyHealth
import com.tianshang.health.core.database.entity.User
import com.tianshang.health.core.database.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class BmiViewModelTest {

    private val userRepository: UserRepository = mockk()
    private val dailyHealthDao: DailyHealthDao = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: BmiViewModel

    private val testUser = User(id = 1, name = "Test", gender = "female", heightCm = 165f)
    private val testHealth = DailyHealth(userId = 1, date = "2026-06-01", weightKg = 65f)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        coEvery { userRepository.getOrCreateDefault() } returns testUser
        coEvery { userRepository.updateHeight(any(), any()) } returns Unit
        coEvery { dailyHealthDao.insert(any()) } returns 1L
        coEvery { dailyHealthDao.update(any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialState loadsData success withRecords`() {
        coEvery { dailyHealthDao.getByUserIdList(testUser.id) } returns listOf(testHealth)

        viewModel = BmiViewModel(userRepository, dailyHealthDao)

        val state = viewModel.uiState.value
        assertTrue(state is BmiUiState.Success)
        with(state as BmiUiState.Success) {
            assertNotNull(currentBmi)
            assertNotNull(category)
            assertEquals(165f, heightCm)
            assertEquals(1, records.size)
        }
    }

    @Test
    fun `initState loadsData success withNoRecords`() {
        coEvery { dailyHealthDao.getByUserIdList(testUser.id) } returns emptyList()

        viewModel = BmiViewModel(userRepository, dailyHealthDao)

        val state = viewModel.uiState.value
        assertTrue(state is BmiUiState.Success)
        with(state as BmiUiState.Success) {
            assertNull(currentBmi)
            assertNull(category)
            assertEquals(165f, heightCm)
            assertTrue(records.isEmpty())
        }
    }

    @Test
    fun `loadData setsError whenNoUser`() {
        coEvery { userRepository.getOrCreateDefault() } throws IllegalStateException("No user")

        viewModel = BmiViewModel(userRepository, dailyHealthDao)

        val state = viewModel.uiState.value
        assertTrue(state is BmiUiState.Error)
    }

    @Test
    fun `saveHeight updatesUserAndRefreshes`() = runTest {
        coEvery { dailyHealthDao.getByUserIdList(testUser.id) } returns emptyList()
        viewModel = BmiViewModel(userRepository, dailyHealthDao)

        viewModel.saveHeight(170f)

        coVerify { userRepository.updateHeight(testUser.id, 170f) }
        coVerify(atLeast = 2) { dailyHealthDao.getByUserIdList(testUser.id) }
    }

    @Test
    fun `addWeightRecord insertsNew whenNoExistingRecord`() = runTest {
        val date = LocalDate.of(2026, 6, 2)
        coEvery { dailyHealthDao.getByUserIdList(testUser.id) } returns emptyList()
        viewModel = BmiViewModel(userRepository, dailyHealthDao)

        coEvery { dailyHealthDao.getByDate(testUser.id, date.toString()) } returns null
        viewModel.addWeightRecord(70f, date)

        coVerify { dailyHealthDao.insert(match { it.weightKg == 70f && it.date == "2026-06-02" }) }
    }

    @Test
    fun `addWeightRecord updatesExisting whenRecordFound`() = runTest {
        val date = LocalDate.of(2026, 6, 1)
        val existing = DailyHealth(id = 5, userId = 1, date = "2026-06-01", weightKg = 65f)
        coEvery { dailyHealthDao.getByUserIdList(testUser.id) } returns emptyList()
        viewModel = BmiViewModel(userRepository, dailyHealthDao)

        coEvery { dailyHealthDao.getByDate(testUser.id, date.toString()) } returns existing
        viewModel.addWeightRecord(68f, date)

        coVerify { dailyHealthDao.update(match { it.weightKg == 68f && it.date == "2026-06-01" && it.id == 5L }) }
    }

    @Test
    fun `categorizeBmi returnsUnderweight`() {
        assertEquals(BmiCategory.UNDERWEIGHT, BmiViewModel.categorizeBmi(16.0f))
    }

    @Test
    fun `categorizeBmi returnsNormal`() {
        assertEquals(BmiCategory.NORMAL, BmiViewModel.categorizeBmi(21.0f))
    }

    @Test
    fun `categorizeBmi returnsOverweight`() {
        assertEquals(BmiCategory.OVERWEIGHT, BmiViewModel.categorizeBmi(26.0f))
    }

    @Test
    fun `categorizeBmi returnsObese`() {
        assertEquals(BmiCategory.OBESE, BmiViewModel.categorizeBmi(30.0f))
    }
}
