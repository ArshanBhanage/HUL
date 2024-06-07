package com.hul.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TimeUtils {

    fun getTimeStampFromDateString(dateString: String): Long {
        // Define the date format
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        // Parse the date string to a Date object
        val date: Date = dateFormat.parse(dateString)

        // Get the timestamp in milliseconds
        return date.time
    }

    fun getMonthFromTimestamp(timestamp: Long): String {
        // Step 1: Convert the timestamp to a Date object
        val date = Date(timestamp)

        // Step 2: Define the date format to get the month in 3-letter format
        val dateFormat = SimpleDateFormat("MMM", Locale.getDefault())

        // Step 3: Format the date to get the month
        return dateFormat.format(date)
    }
}