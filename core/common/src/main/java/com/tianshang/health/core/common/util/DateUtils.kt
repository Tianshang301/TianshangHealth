package com.tianshang.health.core.common.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateUtils {

    private val displayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    fun formatDate(date: LocalDate): String {
        return date.format(displayFormatter)
    }

    fun formatMonth(date: LocalDate): String {
        return date.format(monthFormatter)
    }

    fun parseDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString, displayFormatter)
    }

    fun daysBetween(startDate: LocalDate, endDate: LocalDate): Long {
        return ChronoUnit.DAYS.between(startDate, endDate)
    }

    fun isToday(date: LocalDate): Boolean {
        return date == LocalDate.now()
    }

    fun isFuture(date: LocalDate): Boolean {
        return date.isAfter(LocalDate.now())
    }

    fun isPast(date: LocalDate): Boolean {
        return date.isBefore(LocalDate.now())
    }

    fun addDays(date: LocalDate, days: Long): LocalDate {
        return date.plusDays(days)
    }

    fun subtractDays(date: LocalDate, days: Long): LocalDate {
        return date.minusDays(days)
    }
}
