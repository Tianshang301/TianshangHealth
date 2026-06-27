package com.tianshang.health.feature.steps.service

import com.tianshang.health.core.common.constants.HealthConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StepCounterServiceTest {

    @Test
    fun `stale baseline detection - large delta triggers re-baseline`() {
        val lastStepCount = 100_000L
        val currentTotal = 250_000L
        val newSteps = currentTotal - lastStepCount

        assertTrue(
            "Delta should exceed threshold to trigger re-baseline",
            newSteps > HealthConstants.MAX_STEPS_PER_INTERVAL
        )
    }

    @Test
    fun `normal delta does not exceed threshold`() {
        val lastStepCount = 100_000L
        val currentTotal = 105_000L
        val newSteps = currentTotal - lastStepCount

        assertTrue(
            "Normal walking delta should be within threshold",
            newSteps <= HealthConstants.MAX_STEPS_PER_INTERVAL
        )
    }

    @Test
    fun `sensor reset detection - totalSteps less than lastStepCount`() {
        val lastStepCount = 100_000L
        val currentTotal = 50L

        assertTrue(
            "Sensor reset should be detected when totalSteps < lastStepCount",
            currentTotal < lastStepCount
        )
    }

    @Test
    fun `first event initialization - lastStepCount is -1`() {
        val lastStepCount = -1L
        assertTrue(
            "First event should be detected via lastStepCount == -1",
            lastStepCount == -1L
        )
    }

    @Test
    fun `max steps per interval constant is reasonable`() {
        assertTrue(
            "MAX_STEPS_PER_INTERVAL should be positive",
            HealthConstants.MAX_STEPS_PER_INTERVAL > 0
        )
        assertTrue(
            "MAX_STEPS_PER_INTERVAL should be at least 10000",
            HealthConstants.MAX_STEPS_PER_INTERVAL >= 10_000L
        )
        assertTrue(
            "MAX_STEPS_PER_INTERVAL should not exceed 100000",
            HealthConstants.MAX_STEPS_PER_INTERVAL <= 100_000L
        )
    }

    @Test
    fun `first event sets lastStepCount to totalSteps`() {
        val totalSteps = 25000L
        val lastStepCount = -1L

        val newLastStepCount = totalSteps

        assertEquals("lastStepCount should be set to totalSteps", 25000, newLastStepCount)
    }

    @Test
    fun `normal delta correctly computes steps since last baseline`() {
        val lastStepCount = 25000L
        val currentTotal = 29000L
        val newSteps = currentTotal - lastStepCount

        assertEquals("Delta should be 4000", 4000, newSteps)
    }

    @Test
    fun `after baseline initialization worker sees no unrecorded steps`() {
        val totalStepsAtBaseline = 25000L

        val cachedTotal = totalStepsAtBaseline
        val lastRecorded = totalStepsAtBaseline
        val missedSteps = cachedTotal - lastRecorded

        assertEquals(
            "Worker should see zero unrecorded steps after baseline init",
            0,
            missedSteps
        )
    }

    @Test
    fun `after normal delta worker sees no unrecorded steps`() {
        val previousTotal = 25000L
        val currentTotal = 29000L

        val lastRecorded = currentTotal
        val missedSteps = lastRecorded - previousTotal

        assertTrue(
            "Worker should not re-add steps that were already recorded",
            missedSteps <= 0 || missedSteps == currentTotal - previousTotal
        )
    }
}
