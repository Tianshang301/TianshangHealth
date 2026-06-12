package com.tianshang.health.feature.period.viewmodel

import android.content.Context
import android.content.SharedPreferences
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.security.encryption.KeystoreManager
import com.tianshang.health.feature.period.service.OvulationReminderWorker
import com.tianshang.health.feature.period.service.PeriodReminderWorker
import com.tianshang.health.feature.period.service.PmsReminderWorker
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderViewModelTest {

    private val context: Context = mockk()
    private val stringResolver: StringResolver = mockk()
    private val prefs: SharedPreferences = mockk()
    private val prefsEditor: SharedPreferences.Editor = mockk()
    private lateinit var viewModel: ReminderViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(KeystoreManager)
        every { KeystoreManager.getEncryptedSharedPreferences(context) } returns prefs
        every { prefs.getBoolean("period_reminder_enabled", false) } returns false
        every {
            prefs.getInt("period_reminder_hour", HealthConstants.DEFAULT_PERIOD_REMINDER_HOUR)
        } returns HealthConstants.DEFAULT_PERIOD_REMINDER_HOUR
        every { prefs.getInt("period_reminder_minute", 0) } returns 0
        every { prefs.getBoolean("ovulation_reminder_enabled", false) } returns false
        every {
            prefs.getInt("ovulation_days_before", HealthConstants.DEFAULT_OVULATION_DAYS_BEFORE)
        } returns HealthConstants.DEFAULT_OVULATION_DAYS_BEFORE
        every { prefs.getBoolean("pms_reminder_enabled", false) } returns false
        every {
            prefs.getInt("pms_days_before", HealthConstants.DEFAULT_PMS_DAYS_BEFORE)
        } returns HealthConstants.DEFAULT_PMS_DAYS_BEFORE
        every { prefs.edit() } returns prefsEditor
        every { prefsEditor.putBoolean(any(), any()) } returns prefsEditor
        every { prefsEditor.putInt(any(), any()) } returns prefsEditor
        every { prefsEditor.apply() } returns Unit

        every { stringResolver.getString(R.string.error_failed_load_settings) } returns "Failed to load"
        every { stringResolver.getString(R.string.error_failed_update_settings) } returns "Failed to update"

        mockkObject(PeriodReminderWorker.Companion)
        every { PeriodReminderWorker.scheduleReminder(any(), any(), any()) } answers { Unit }
        every { PeriodReminderWorker.cancelReminder(any()) } answers { Unit }

        mockkObject(OvulationReminderWorker.Companion)
        every { OvulationReminderWorker.scheduleReminder(any()) } answers { Unit }
        every { OvulationReminderWorker.cancelReminder(any()) } answers { Unit }

        mockkObject(PmsReminderWorker.Companion)
        every { PmsReminderWorker.scheduleReminder(any()) } answers { Unit }
        every { PmsReminderWorker.cancelReminder(any()) } answers { Unit }

        viewModel = ReminderViewModel(context, stringResolver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initial_state_is_success_with_defaults() {
        val state = viewModel.uiState.value
        assert(state is ReminderUiState.Success)
        val settings = (state as ReminderUiState.Success).settings
        assert(!settings.periodReminderEnabled)
    }

    @Test
    fun togglePeriodReminder_saves_and_schedules_worker() = runTest {
        every {
            prefs.getInt("period_reminder_hour", HealthConstants.DEFAULT_PERIOD_REMINDER_HOUR)
        } returns HealthConstants.DEFAULT_PERIOD_REMINDER_HOUR
        every { prefs.getInt("period_reminder_minute", 0) } returns 0
        every { prefs.getBoolean("period_reminder_enabled", false) } returnsMany listOf(false, true)

        viewModel.togglePeriodReminder(true)

        verify { PeriodReminderWorker.scheduleReminder(context, HealthConstants.DEFAULT_PERIOD_REMINDER_HOUR, 0) }
    }

    @Test
    fun togglePeriodReminder_cancels_worker_when_disabled() = runTest {
        every { prefs.getBoolean("period_reminder_enabled", false) } returnsMany listOf(true, false)

        viewModel.togglePeriodReminder(false)

        verify { PeriodReminderWorker.cancelReminder(context) }
    }

    @Test
    fun toggleOvulationReminder_schedules_worker() = runTest {
        every { prefs.getBoolean("ovulation_reminder_enabled", false) } returnsMany listOf(false, true)

        viewModel.toggleOvulationReminder(true)

        verify { OvulationReminderWorker.scheduleReminder(context) }
    }

    @Test
    fun togglePmsReminder_schedules_worker() = runTest {
        every { prefs.getBoolean("pms_reminder_enabled", false) } returnsMany listOf(false, true)

        viewModel.togglePmsReminder(true)

        verify { PmsReminderWorker.scheduleReminder(context) }
    }

    @Test
    fun setPeriodReminderTime_updates_and_reschedules() = runTest {
        every { prefs.getBoolean("period_reminder_enabled", false) } returns true
        every { prefs.getInt("period_reminder_hour", HealthConstants.DEFAULT_PERIOD_REMINDER_HOUR) } returns 14
        every { prefs.getInt("period_reminder_minute", 0) } returns 30

        viewModel.setPeriodReminderTime(14, 30)

        verify { PeriodReminderWorker.scheduleReminder(context, 14, 30) }
    }
}
