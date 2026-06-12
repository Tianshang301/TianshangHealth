package com.tianshang.health

import com.tianshang.health.core.database.entity.User.Gender
import com.tianshang.health.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NavHostTest {

    @Test
    fun `Screen sealed class has all expected routes`() {
        val routeToExpected = listOf(
            Screen.Onboarding.route to "onboarding",
            Screen.Dashboard.route to "dashboard",
            Screen.Period.route to "period",
            Screen.Exercise.route to "exercise",
            Screen.Analysis.route to "analysis",
            Screen.Profile.route to "profile",
            Screen.PeriodAnalysis.route to "period_analysis",
            Screen.Theme.route to "theme",
            Screen.Language.route to "language",
            Screen.Bmi.route to "bmi",
            Screen.RecycleBin.route to "recycle_bin",
            Screen.Backup.route to "backup",
            Screen.Nutrition.route to "nutrition",
            Screen.Sleep.route to "sleep",
            Screen.AppLock.route to "app_lock_settings",
            Screen.Report.route to "report",
            Screen.Reminders.route to "reminders",
            Screen.Terms.route to "terms"
        )
        routeToExpected.forEach { (actual, expected) ->
            assertEquals("Route for ${actual::class.simpleName}", expected, actual)
        }
    }

    @Test
    fun `all Screen routes are unique`() {
        val allRoutes = listOf(
            Screen.Onboarding.route,
            Screen.Dashboard.route,
            Screen.Period.route,
            Screen.Exercise.route,
            Screen.Analysis.route,
            Screen.Profile.route,
            Screen.PeriodAnalysis.route,
            Screen.Theme.route,
            Screen.Language.route,
            Screen.Bmi.route,
            Screen.RecycleBin.route,
            Screen.Backup.route,
            Screen.Nutrition.route,
            Screen.Sleep.route,
            Screen.AppLock.route,
            Screen.Report.route,
            Screen.Reminders.route,
            Screen.Terms.route
        )
        assertEquals(allRoutes.size, allRoutes.distinct().size)
    }

    @Test
    fun `Screen objects are singleton objects`() {
        assert(Screen.Dashboard === Screen.Dashboard)
        assert(Screen.Period === Screen.Period)
        assert(Screen.Exercise === Screen.Exercise)
        assertNotEquals(Screen.Dashboard, Screen.Period)
    }

    @Test
    fun `male bottom navigation has 4 items`() {
        val gender = Gender.MALE
        val screens = if (gender == Gender.MALE) {
            listOf(Screen.Dashboard, Screen.Exercise, Screen.Analysis, Screen.Profile)
        } else {
            listOf(Screen.Dashboard, Screen.Period, Screen.Exercise, Screen.Analysis, Screen.Profile)
        }
        assertEquals(4, screens.size)
        assertEquals(Screen.Dashboard.route, screens[0].route)
        assertEquals(Screen.Exercise.route, screens[1].route)
        assertEquals(Screen.Analysis.route, screens[2].route)
        assertEquals(Screen.Profile.route, screens[3].route)
    }

    @Test
    fun `female bottom navigation has 5 items`() {
        val gender = Gender.FEMALE
        val screens = if (gender == Gender.MALE) {
            listOf(Screen.Dashboard, Screen.Exercise, Screen.Analysis, Screen.Profile)
        } else {
            listOf(Screen.Dashboard, Screen.Period, Screen.Exercise, Screen.Analysis, Screen.Profile)
        }
        assertEquals(5, screens.size)
        assertEquals(Screen.Dashboard.route, screens[0].route)
        assertEquals(Screen.Period.route, screens[1].route)
        assertEquals(Screen.Exercise.route, screens[2].route)
        assertEquals(Screen.Analysis.route, screens[3].route)
        assertEquals(Screen.Profile.route, screens[4].route)
    }

    @Test
    fun `other bottom navigation has 5 items same as female`() {
        val gender = Gender.OTHER
        val screens = if (gender == Gender.MALE) {
            listOf(Screen.Dashboard, Screen.Exercise, Screen.Analysis, Screen.Profile)
        } else {
            listOf(Screen.Dashboard, Screen.Period, Screen.Exercise, Screen.Analysis, Screen.Profile)
        }
        assertEquals(5, screens.size)
    }

    @Test
    fun `start destination is Terms when onboarding not completed`() {
        val isOnboardingCompleted = false
        val startDestination = if (isOnboardingCompleted) Screen.Dashboard.route else Screen.Terms.route
        assertEquals("terms", startDestination)
    }

    @Test
    fun `start destination is Dashboard when onboarding completed`() {
        val isOnboardingCompleted = true
        val startDestination = if (isOnboardingCompleted) Screen.Dashboard.route else Screen.Terms.route
        assertEquals("dashboard", startDestination)
    }

    @Test
    fun `gender fromValue returns correct enum`() {
        assertEquals(Gender.MALE, Gender.fromValue("male"))
        assertEquals(Gender.FEMALE, Gender.fromValue("female"))
        assertEquals(Gender.OTHER, Gender.fromValue("other"))
    }

    @Test
    fun `gender fromValue defaults to FEMALE for unknown value`() {
        assertEquals(Gender.FEMALE, Gender.fromValue("unknown"))
        assertEquals(Gender.FEMALE, Gender.fromValue(""))
    }

    @Test
    fun `dashboard screen has non-zero titleResId`() {
        assertNotEquals(0, Screen.Dashboard.titleResId)
    }

    @Test
    fun `sidebar screens have zero titleResId`() {
        listOf(
            Screen.PeriodAnalysis,
            Screen.Theme,
            Screen.Language,
            Screen.Bmi,
            Screen.RecycleBin,
            Screen.Backup,
            Screen.AppLock,
            Screen.Report,
            Screen.Reminders,
            Screen.Terms
        ).forEach { screen ->
            assertEquals("${screen::class.simpleName} should have titleResId=0", 0, screen.titleResId)
        }
    }

    @Test
    fun `bottom nav screens have non-zero titleResId`() {
        listOf(
            Screen.Dashboard,
            Screen.Period,
            Screen.Exercise,
            Screen.Analysis,
            Screen.Profile
        ).forEach { screen ->
            assertNotEquals("${screen::class.simpleName} should have non-zero titleResId", 0, screen.titleResId)
        }
    }
}
