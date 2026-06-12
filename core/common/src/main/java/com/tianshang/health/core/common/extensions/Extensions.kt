package com.tianshang.health.core.common.extensions

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun LocalDate.toDisplayString(): String {
    return this.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}

fun LocalDate.toMonthString(): String {
    return this.format(DateTimeFormatter.ofPattern("yyyy-MM"))
}

fun LocalDate.isSameDay(other: LocalDate): Boolean {
    return this == other
}

fun LocalDate.isBetween(start: LocalDate, end: LocalDate): Boolean {
    return !this.isBefore(start) && !this.isAfter(end)
}

fun Int.toFlowLevelString(): String {
    return when (this) {
        1 -> "Light"
        2 -> "Medium"
        3 -> "Heavy"
        else -> "Unknown"
    }
}

fun Int.toPainLevelString(): String {
    return when (this) {
        0 -> "None"
        1 -> "Mild"
        2 -> "Moderate"
        3 -> "Severe"
        else -> "Unknown"
    }
}
