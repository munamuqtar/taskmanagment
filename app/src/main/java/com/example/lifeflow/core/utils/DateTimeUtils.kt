package com.example.lifeflow.core.utils

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeUtils {
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    private val DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
    private val TIME_FORMATTER_12H = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
    private val TIME_FORMATTER_24H = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    fun getCurrentDateString(): String {
        return LocalDate.now().format(DATE_FORMATTER)
    }

    fun formatDateForDisplay(dateString: String?): String {
        if (dateString.isNullOrBlank()) return ""
        return try {
            val date = LocalDate.parse(dateString, DATE_FORMATTER)
            date.format(DISPLAY_DATE_FORMATTER)
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatTimeForDisplay(timeString: String?, is12Hour: Boolean = true): String {
        if (timeString.isNullOrBlank()) return ""
        return try {
            val time = LocalTime.parse(timeString)
            if (is12Hour) time.format(TIME_FORMATTER_12H) else time.format(TIME_FORMATTER_24H)
        } catch (e: Exception) {
            timeString
        }
    }

    fun isOverdue(dateString: String?, timeString: String?): Boolean {
        if (dateString.isNullOrBlank()) return false
        return try {
            val taskDate = LocalDate.parse(dateString, DATE_FORMATTER)
            val today = LocalDate.now()
            if (taskDate.isBefore(today)) return true
            if (taskDate.isEqual(today) && !timeString.isNullOrBlank()) {
                val taskTime = LocalTime.parse(timeString)
                return LocalTime.now().isAfter(taskTime)
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    fun getGreetingMessage(): String {
        val hour = LocalTime.now().hour
        return when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Good Night"
        }
    }
}