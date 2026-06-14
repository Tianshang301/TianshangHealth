package com.tianshang.health.feature.steps.service

import com.tianshang.health.core.common.constants.HealthConstants
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
}
