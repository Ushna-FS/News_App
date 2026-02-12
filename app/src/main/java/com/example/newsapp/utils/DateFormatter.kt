package com.example.newsapp.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DateFormatter @Inject constructor() {

    private val inputFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

    private val outputFormat = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault())

    private val dateOnlyFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun formatDisplayDate(publishedAt: String?): String {
        return try {
            if (publishedAt.isNullOrEmpty()) return ""

            val date = inputFormat.parse(publishedAt) ?: return ""
            outputFormat.format(date)
        } catch (e: Exception) {
            try {
                // Fallback: try to parse just the date part
                val datePart = publishedAt?.substringBefore("T") ?: return ""
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(datePart)
                dateOnlyFormat.format(date ?: Date())
            } catch (e: Exception) {
                publishedAt?.substringBefore("T") ?: ""
            }
        }
    }

    fun parseToTimestamp(dateString: String?): Long {
        return try {
            if (dateString.isNullOrEmpty()) return 0L

            inputFormat.parse(dateString)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun getTimeAgo(publishedAt: String?): String {
        val date = try {
            inputFormat.parse(publishedAt ?: return "")
        } catch (e: Exception) {
            return publishedAt?.substringBefore("T") ?: ""
        } ?: return ""

        val now = Date()
        val diff = now.time - date.time
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            seconds < 60 -> "$seconds seconds ago"
            minutes < 60 -> "$minutes minutes ago"
            hours < 24 -> "$hours hours ago"
            days < 7 -> "$days days ago"
            else -> dateOnlyFormat.format(date)
        }
    }
}