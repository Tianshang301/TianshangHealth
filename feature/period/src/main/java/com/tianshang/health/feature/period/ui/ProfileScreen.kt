package com.tianshang.health.feature.period.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tianshang.health.core.common.R
import com.tianshang.health.core.database.entity.User
import com.tianshang.health.feature.period.viewmodel.ProfileViewModel
import java.util.Calendar

private fun genderDisplayNameRes(gender: User.Gender): Int = when (gender) {
    User.Gender.MALE -> R.string.gender_male
    User.Gender.FEMALE -> R.string.gender_female
    User.Gender.OTHER -> R.string.gender_other
}

private enum class DobMode { EXACT, YEAR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onNavigateToTheme: () -> Unit = {},
    onNavigateToLanguage: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    onNavigateToRecycleBin: () -> Unit = {},
    onNavigateToBmi: () -> Unit = {},
    onNavigateToAppLock: () -> Unit = {}
) {
    val currentGender by viewModel.currentGender.collectAsState()
    val showGenderDialog by viewModel.showGenderDialog.collectAsState()
    val currentDateOfBirth by viewModel.currentDateOfBirth.collectAsState()
    var dobMode by remember { mutableStateOf(DobMode.EXACT) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.genderChanged.collect {
            (context as? AppCompatActivity)?.recreate()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.profile_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.section_appearance),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard(
            icon = Icons.Default.ColorLens,
            title = stringResource(R.string.theme_customization),
            description = stringResource(R.string.profile_theme_description)
        ) {
            Button(onClick = onNavigateToTheme) {
                Text(stringResource(R.string.open))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(
            icon = Icons.Default.Person,
            title = stringResource(R.string.gender_setting_title),
            description = stringResource(R.string.gender_setting_description)
        ) {
            Text(
                text = stringResource(genderDisplayNameRes(currentGender)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Button(onClick = { viewModel.toggleGenderDialog() }) {
                Text(stringResource(R.string.open))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(
            icon = Icons.Default.Language,
            title = stringResource(R.string.language),
            description = stringResource(R.string.profile_language_description)
        ) {
            Button(onClick = onNavigateToLanguage) {
                Text(stringResource(R.string.open))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.section_health),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard(
            icon = Icons.Default.Favorite,
            title = stringResource(R.string.bmi_title),
            description = stringResource(R.string.profile_bmi_description)
        ) {
            Button(onClick = onNavigateToBmi) {
                Text(stringResource(R.string.open))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Date of Birth
        val dobDescription = if (dobMode == DobMode.YEAR) {
            currentDateOfBirth?.substringBefore("-")
        } else {
            currentDateOfBirth
        } ?: stringResource(R.string.profile_dob_not_set)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.profile_dob_title),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Text(
                            text = dobDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                    Button(onClick = {
                        if (dobMode == DobMode.EXACT) {
                            showDatePicker = true
                        } else {
                            showYearPicker = true
                        }
                    }) {
                        Text(stringResource(R.string.edit))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = dobMode == DobMode.EXACT,
                        onClick = { dobMode = DobMode.EXACT },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text(text = stringResource(R.string.exact_date_mode), maxLines = 1)
                    }
                    SegmentedButton(
                        selected = dobMode == DobMode.YEAR,
                        onClick = { dobMode = DobMode.YEAR },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text(text = stringResource(R.string.year_only_mode), maxLines = 1)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ProfileSecuritySection(onNavigateToAppLock = onNavigateToAppLock)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.section_data),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard(
            icon = Icons.Default.Backup,
            title = stringResource(R.string.backup_restore_title),
            description = stringResource(R.string.profile_backup_description)
        ) {
            Button(onClick = onNavigateToBackup) {
                Text(stringResource(R.string.open))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(
            icon = Icons.Default.Delete,
            title = stringResource(R.string.recycle_bin_title),
            description = stringResource(R.string.profile_recycle_bin_description)
        ) {
            Button(onClick = onNavigateToRecycleBin) {
                Text(stringResource(R.string.open))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.about),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.profile_version),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.profile_app_description),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentDateOfBirth?.let {
                try {
                    java.time.LocalDate.parse(it).atStartOfDay(java.time.ZoneId.of("UTC"))
                        .toInstant().toEpochMilli()
                } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (_: Exception) { null }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.of("UTC"))
                                .toLocalDate()
                                .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                            viewModel.updateDateOfBirth(date)
                        }
                        showDatePicker = false
                    }
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.updateDateOfBirth(null)
                    showDatePicker = false
                }) { Text(stringResource(R.string.clear)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showYearPicker) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val initialYear = currentDateOfBirth?.let {
            try { it.substringBefore("-").toInt() } catch (
                e: kotlinx.coroutines.CancellationException
            ) { throw e } catch (_: Exception) { currentYear - 25 }
        } ?: (currentYear - 25)
        YearPickerBottomSheet(
            visible = showYearPicker,
            initialYear = initialYear.coerceIn(1900, currentYear),
            maxYear = currentYear,
            onDismiss = { showYearPicker = false },
            onConfirm = { year ->
                viewModel.updateDateOfBirth("$year-01-01")
                showYearPicker = false
            }
        )
    }

    if (showGenderDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleGenderDialog() },
            title = { Text(stringResource(R.string.select_gender)) },
            text = {
                Column {
                    User.Gender.entries.forEach { gender ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentGender == gender,
                                onClick = { viewModel.updateGender(gender) }
                            )
                            Text(
                                text = stringResource(genderDisplayNameRes(gender)),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.toggleGenderDialog() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
