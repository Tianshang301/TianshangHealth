package com.tianshang.health.feature.sleep.domain

class SleepPhaseDetector {

    fun estimateDeepSleepWindow(bedTime: String?, wakeTime: String?): SleepPhase? {
        if (bedTime == null || wakeTime == null) return null

        val bedMinutes = parseMinutes(bedTime) ?: return null
        val wakeMinutes = parseMinutes(wakeTime) ?: return null

        var totalMinutes = wakeMinutes - bedMinutes
        if (totalMinutes < 0) totalMinutes += 24 * 60
        if (totalMinutes < 180) return null

        val deepStart = (bedMinutes + totalMinutes * 0.25f).toInt() % (24 * 60)
        val deepEnd = (bedMinutes + totalMinutes * 0.65f).toInt() % (24 * 60)

        return SleepPhase(
            startHour = deepStart / 60,
            endHour = deepEnd / 60,
            phase = PhaseType.DEEP_SLEEP
        )
    }

    private fun parseMinutes(time: String): Int? {
        val parts = time.split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h * 60 + m
    }
}
