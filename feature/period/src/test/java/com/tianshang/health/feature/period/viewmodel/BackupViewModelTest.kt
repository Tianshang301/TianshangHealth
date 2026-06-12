package com.tianshang.health.feature.period.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.feature.period.backup.EncryptedBackupManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalCoroutinesApi::class)
class BackupViewModelTest {

    private val context: Context = mockk()
    private val contentResolver: ContentResolver = mockk()
    private val stringResolver: StringResolver = mockk()
    private val backupManager: EncryptedBackupManager = mockk()
    private lateinit var viewModel: BackupViewModel

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testUri: Uri = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { context.contentResolver } returns contentResolver
        every { stringResolver.getString(R.string.backup_exported_success) } returns "Backup exported"
        every { stringResolver.getString(R.string.backup_fail) } returns "Backup failed"
        every { stringResolver.getString(R.string.import_success_format, any<Int>()) } returns "Imported records"
        every { stringResolver.getString(R.string.restore_fail) } returns "Restore failed"
        every { stringResolver.getString(R.string.csv_header) } returns "Date,Type,Value"
        every { stringResolver.getString(R.string.csv_exported_success) } returns "CSV exported"
        every { stringResolver.getString(R.string.csv_export_failed) } returns "CSV export failed"
        viewModel = BackupViewModel(context, stringResolver, backupManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initial_state_is_idle() {
        assert(viewModel.uiState.value is BackupUiState.Idle)
    }

    @Test
    fun exportBackup_sets_success() = runTest {
        val outputStream: OutputStream = mockk(relaxed = true)
        every { contentResolver.openOutputStream(testUri) } returns outputStream
        coEvery { backupManager.exportBackup(any(), any()) } returns Unit

        viewModel.exportBackup(testUri, "password")

        assert(viewModel.uiState.value is BackupUiState.Success)
    }

    @Test
    fun exportBackup_sets_error_on_failure() = runTest {
        every { contentResolver.openOutputStream(testUri) } returns null

        viewModel.exportBackup(testUri, "password")

        assert(viewModel.uiState.value is BackupUiState.Error)
    }

    @Test
    fun importBackup_sets_importing_then_success() = runTest {
        val inputStream: InputStream = mockk()
        every { contentResolver.openInputStream(testUri) } returns inputStream
        coEvery { backupManager.importBackup(any(), any()) } returns 5

        viewModel.importBackup(testUri, "password")

        assert(viewModel.uiState.value is BackupUiState.Success)
    }

    @Test
    fun importBackup_sets_error_on_failure() = runTest {
        every { contentResolver.openInputStream(testUri) } returns null

        viewModel.importBackup(testUri, "password")

        assert(viewModel.uiState.value is BackupUiState.Error)
    }

    @Test
    fun exportCsv_writes_and_succeeds() = runTest {
        val outputStream: OutputStream = mockk(relaxed = true)
        every { contentResolver.openOutputStream(testUri) } returns outputStream

        viewModel.exportCsv(testUri)

        assert(viewModel.uiState.value is BackupUiState.Success)
    }

    @Test
    fun resetState_returns_to_idle() {
        viewModel.resetState()
        assert(viewModel.uiState.value is BackupUiState.Idle)
    }
}
