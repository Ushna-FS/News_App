package com.example.shared.utils

import kotlinx.datetime.*


class DateFormatter {
    fun formatDisplayDate(publishedAt: String?): String {
        if (publishedAt.isNullOrBlank()) return ""

        return try {
            val instant = Instant.parse(publishedAt)
            val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

            val day = dateTime.dayOfMonth.toString().padStart(2, '0')
            val month = dateTime.month.name.lowercase()
                .replaceFirstChar { it.uppercase() }
            val year = dateTime.year

            val hour = (dateTime.hour % 12).let { if (it == 0) 12 else it }
            val minute = dateTime.minute.toString().padStart(2, '0')
            val amPm = if (dateTime.hour < 12) "AM" else "PM"

            "$day $month $year, $hour:$minute $amPm"

        } catch (_: Exception) {
            try {
                val datePart = publishedAt.substringBefore("T")
                val localDate = LocalDate.parse(datePart)

                val day = localDate.dayOfMonth.toString().padStart(2, '0')
                val month = localDate.month.name.lowercase()
                    .replaceFirstChar { it.uppercase() }
                val year = localDate.year

                "$day $month $year"

            } catch (_: Exception) {
                publishedAt.substringBefore("T")
            }
        }
    }

    fun parseToTimestamp(dateString: String?): Long {
        return try {
            if (dateString.isNullOrEmpty()) return 0L

            Instant.parse(dateString).toEpochMilliseconds()
        } catch (_: Exception) {
            0L
        }
    }

    fun getTimeAgo(timestamp: Long): String {
        val now = Clock.System.now()
        val diff = now.toEpochMilliseconds() - timestamp

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes mins ago"
            hours < 24 -> "$hours hrs ago"
            days < 7 -> "$days days ago"
            else -> "$days days ago"
        }
    }
}