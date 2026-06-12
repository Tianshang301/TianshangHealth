package com.tianshang.health.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tianshang.health.core.common.R
import com.tianshang.health.core.security.auth.AppLockManager
import com.tianshang.health.core.security.encryption.KeystoreManager
import com.tianshang.health.feature.analysis.ui.AnalysisScreen
import com.tianshang.health.feature.analysis.ui.ReportScreen
import com.tianshang.health.feature.dashboard.ui.DashboardScreen
import com.tianshang.health.feature.nutrition.ui.NutritionScreen
import com.tianshang.health.feature.onboarding.model.Gender
import com.tianshang.health.feature.onboarding.ui.GenderSelectionScreen
import com.tianshang.health.feature.period.ui.AppLockScreen
import com.tianshang.health.feature.period.ui.AppLockSettingsScreen
import com.tianshang.health.feature.period.ui.BackupScreen
import com.tianshang.health.feature.period.ui.BmiScreen
import com.tianshang.health.feature.period.ui.LanguageScreen
import com.tianshang.health.feature.period.ui.PeriodScreen
import com.tianshang.health.feature.period.ui.RecycleBinScreen
import com.tianshang.health.feature.period.ui.RemindersScreen
import com.tianshang.health.feature.period.ui.TermsScreen
import com.tianshang.health.feature.period.ui.ThemeScreen
import com.tianshang.health.feature.sleep.ui.SleepScreen
import com.tianshang.health.feature.period.ui.AnalysisScreen as PeriodAnalysisScreen

sealed class Screen(
    val route: String,
    val titleResId: Int,
    val icon: ImageVector
) {
    object Onboarding : Screen("onboarding", 0, Icons.Default.Person)
    object Dashboard : Screen("dashboard", R.string.nav_dashboard, Icons.Default.Dashboard)
    object Period : Screen("period", R.string.nav_period, Icons.Default.Female)
    object Exercise : Screen("exercise", R.string.nav_fitness, Icons.Default.DirectionsWalk)
    object Analysis : Screen("analysis", R.string.nav_analysis, Icons.Default.Analytics)
    object Profile : Screen("profile", R.string.nav_profile, Icons.Default.Person)
    object PeriodAnalysis : Screen("period_analysis", 0, Icons.Default.Analytics)
    object Theme : Screen("theme", 0, Icons.Default.ColorLens)
    object Language : Screen("language", 0, Icons.Default.Language)
    object Bmi : Screen("bmi", 0, Icons.Default.Favorite)
    object RecycleBin : Screen("recycle_bin", 0, Icons.Default.Delete)
    object Backup : Screen("backup", 0, Icons.Default.Backup)
    object Nutrition : Screen("nutrition", 0, Icons.Default.Favorite)
    object Sleep : Screen("sleep", 0, Icons.Default.Bedtime)
    object AppLock : Screen("app_lock_settings", 0, Icons.Default.Favorite)
    object Report : Screen("report", 0, Icons.Default.Analytics)
    object Reminders : Screen("reminders", 0, Icons.Default.Favorite)
    object Terms : Screen("terms", 0, Icons.Default.Person)
}

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isLocked by AppLockManager.isLocked.collectAsState()
    val isAppLockEnabled by AppLockManager.isEnabled.collectAsState()

    val prefs = KeystoreManager.getEncryptedSharedPreferences(context)

    val isOnboardingCompleted = remember(currentRoute) {
        prefs.getBoolean("onboarding_completed", false)
    }
    val userGender = remember(currentRoute) {
        val genderValue = prefs.getString("user_gender", Gender.FEMALE.value) ?: Gender.FEMALE.value
        Gender.fromValue(genderValue)
    }

    val screens = if (userGender == Gender.MALE) {
        listOf(Screen.Dashboard, Screen.Exercise, Screen.Analysis, Screen.Profile)
    } else {
        listOf(Screen.Dashboard, Screen.Period, Screen.Exercise, Screen.Analysis, Screen.Profile)
    }

    val startDestination = if (isOnboardingCompleted) Screen.Dashboard.route else Screen.Terms.route

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (isOnboardingCompleted) {
                    NavigationBar {
                        screens.forEach { screen ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = if (screen.titleResId != 0) {
                                            stringResource(screen.titleResId)
                                        } else {
                                            screen.route
                                        }
                                    )
                                },
                                label = {
                                    Text(
                                        text = when (screen) {
                                            Screen.Dashboard -> stringResource(R.string.nav_dashboard)
                                            Screen.Period -> stringResource(R.string.nav_period)
                                            Screen.Exercise -> stringResource(R.string.nav_fitness)
                                            Screen.Analysis -> stringResource(R.string.nav_analysis)
                                            Screen.Profile -> stringResource(R.string.nav_profile)
                                            else -> if (screen.titleResId != 0) {
                                                stringResource(screen.titleResId)
                                            } else {
                                                screen.route
                                            }
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                selected = currentRoute == screen.route,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Terms.route) {
                    TermsScreen(
                        onAccept = {
                            navController.navigate(Screen.Onboarding.route) {
                                popUpTo(Screen.Terms.route) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Screen.Onboarding.route) {
                    GenderSelectionScreen(
                        onGenderSelected = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        onNavigateToPeriod = {
                            navController.navigate(Screen.Period.route)
                        },
                        onNavigateToSteps = {
                            navController.navigate(Screen.Exercise.route)
                        },
                        onNavigateToHealthData = {
                            navController.navigate(Screen.Analysis.route)
                        },
                        onNavigateToNutrition = {
                            navController.navigate(Screen.Nutrition.route)
                        },
                        onNavigateToSleep = {
                            navController.navigate(Screen.Sleep.route)
                        }
                    )
                }
                composable(Screen.Period.route) {
                    PeriodScreen(
                        onNavigateToReminders = {
                            navController.navigate(Screen.Reminders.route)
                        }
                    )
                }
                composable(Screen.Reminders.route) {
                    RemindersScreen()
                }
                composable(Screen.Exercise.route) {
                    ExerciseScreen(
                        onNavigateToPeriodAnalysis = {
                            navController.navigate(Screen.PeriodAnalysis.route)
                        }
                    )
                }
                composable(Screen.PeriodAnalysis.route) {
                    PeriodAnalysisScreen()
                }
                composable(Screen.Analysis.route) {
                    AnalysisScreen(
                        onNavigateToReport = {
                            navController.navigate(Screen.Report.route)
                        }
                    )
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onNavigateToTheme = {
                            navController.navigate(Screen.Theme.route)
                        },
                        onNavigateToLanguage = {
                            navController.navigate(Screen.Language.route)
                        },
                        onNavigateToBmi = {
                            navController.navigate(Screen.Bmi.route)
                        },
                        onNavigateToRecycleBin = {
                            navController.navigate(Screen.RecycleBin.route)
                        },
                        onNavigateToBackup = {
                            navController.navigate(Screen.Backup.route)
                        },
                        onNavigateToAppLock = {
                            navController.navigate(Screen.AppLock.route)
                        }
                    )
                }
                composable(Screen.Theme.route) {
                    ThemeScreen()
                }
                composable(Screen.Language.route) {
                    LanguageScreen()
                }
                composable(Screen.Bmi.route) {
                    BmiScreen()
                }
                composable(Screen.RecycleBin.route) {
                    RecycleBinScreen()
                }
                composable(Screen.Backup.route) {
                    BackupScreen()
                }
                composable(Screen.Nutrition.route) {
                    NutritionScreen()
                }
                composable(Screen.Sleep.route) {
                    SleepScreen()
                }
                composable(Screen.AppLock.route) {
                    AppLockSettingsScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.Report.route) {
                    ReportScreen()
                }
            }
        }

        if (isLocked && isAppLockEnabled) {
            AppLockScreen(
                onAuthenticated = { /* handled by AppLockManager unlock in ViewModel */ },
                modifier = Modifier
                    .zIndex(10f)
                    .fillMaxSize()
            )
        }
    }
}

@Composable
private fun ProfileScreen(
    onNavigateToTheme: () -> Unit = {},
    onNavigateToLanguage: () -> Unit = {},
    onNavigateToBmi: () -> Unit = {},
    onNavigateToRecycleBin: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    onNavigateToAppLock: () -> Unit = {}
) {
    com.tianshang.health.feature.period.ui.ProfileScreen(
        onNavigateToTheme = onNavigateToTheme,
        onNavigateToLanguage = onNavigateToLanguage,
        onNavigateToBmi = onNavigateToBmi,
        onNavigateToRecycleBin = onNavigateToRecycleBin,
        onNavigateToBackup = onNavigateToBackup,
        onNavigateToAppLock = onNavigateToAppLock
    )
}
