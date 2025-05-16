package com.example.brokskeeping.Functionality

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.brokskeeping.DataClasses.DateRange
import com.example.brokskeeping.DataClasses.MaxMins
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object Utils {

    fun parseStringToListOfIntegers(string: String): List<Int> {
        return string
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
    }

    fun getNewData(data: String, firstDateTime: Long, lastDateTime: Long): String {
        val lines = data.split("\n")
        val filteredLines = lines.filter { line ->
            val parts = line.split(", ")
            if (parts.size == 3) {
                val timeString = parts[0]
                val time = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).parse(timeString)?.time ?: 0L
                time in firstDateTime..lastDateTime
            } else {
                false
            }
        }
        return filteredLines.joinToString("\n")
    }

    fun isMoreThanDay(date1: Date, date2: Date):Boolean {
        val differenceInMillis = abs(date2.time - date1.time)

        // Calculate the duration of 24 hours in milliseconds
        val oneDayInMillis = 24 * 60 * 60 * 1000L // 24 hours * 60 minutes * 60 seconds * 1000 milliseconds

        // Check if the difference is greater than 24 hours
        return differenceInMillis > oneDayInMillis
    }

    fun getMaxMinTempHum(data: String): MaxMins {
        val lines = data.split("\n")
        var maxTemp = Double.MIN_VALUE
        var minTemp = Double.MAX_VALUE
        var maxHum = Double.MIN_VALUE
        var minHum = Double.MAX_VALUE

        for (line in lines) {
            val values = line.split(", ")
            if (values.size == 3) {
                val temp = values[1].toDouble()
                val hum = values[2].toDouble()
                maxTemp = maxOf(maxTemp, temp)
                minTemp = minOf(minTemp, temp)
                maxHum = maxOf(maxHum, hum)
                minHum = minOf(minHum, hum)
            }
        }
        return MaxMins(maxTemp, minTemp, maxHum, minHum)
    }

    fun getFirstAndLastDate(logData: String): DateRange {
        val lines = logData.trim().lines()
        if (lines.isEmpty()) {
            throw IllegalArgumentException("Log data is empty")
        }

        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val firstLineDate = extractDate(lines[1], dateFormat)
        val lastLineDate = extractDate(lines.last(), dateFormat)

        return DateRange(firstLineDate, lastLineDate)
    }

    private fun extractDate(line: String, dateFormat: SimpleDateFormat): Date {
        val parts = line.split(", ")
        if (parts.isEmpty()) {
            throw IllegalArgumentException("Invalid log data format")
        }

        val dateString = parts.first()
        return dateFormat.parse(dateString) ?: throw IllegalArgumentException("Invalid date format: $dateString")
    }

    fun showConfirmationDialog(context: Context, message: String, callback: (Boolean) -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmation")
        builder.setMessage(message)

        builder.setPositiveButton("Yes") { _, _ ->
            callback.invoke(true)
        }

        builder.setNegativeButton("No") { _, _ ->
            callback.invoke(false)
        }

        builder.show()
    }
    fun correctHiveCount(number: Int): Boolean {
        return number in 0..100
    }


    fun notesFormat(notes: String): Boolean {
        return true
    }

    fun isValidFile(fileName: String, fileData: String): Boolean {
        return fileName.endsWith(".txt") || fileName == "None"
    }

    fun getStartAndEndTime(year: Int? = null, month: Int? = null): Pair<Pair<Long?, Long?>, Int> {
        val calendar = Calendar.getInstance()
        var startTime: Long? = null
        var endTime: Long? = null

        when {
            (year == null) && (month == null) -> {
                startTime = 0L
                endTime = System.currentTimeMillis()
            }
            year != null && year > 0 && (month == null) -> {
                calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0)
                startTime = calendar.timeInMillis
                calendar.set(year, Calendar.DECEMBER, 31, 23, 59, 59)
                endTime = calendar.timeInMillis
            }
            year != null && year > 0 && month != null && month in 1..12 -> {
                calendar.set(year, month - 1, 1, 0, 0, 0)
                startTime = calendar.timeInMillis
                calendar.set(year, month - 1, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
                endTime = calendar.timeInMillis
            }
            else -> return Pair(Pair(null, null), 0)
        }
        return Pair(Pair(startTime, endTime), 1)
    }
}