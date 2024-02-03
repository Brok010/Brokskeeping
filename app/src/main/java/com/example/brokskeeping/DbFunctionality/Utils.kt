package com.example.brokskeeping.DbFunctionality

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.brokskeeping.DataClasses.DateRange
import com.example.brokskeeping.DataClasses.MaxMins
import com.example.brokskeeping.DataClasses.SensorData
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {

    fun compressData(data: String, firstDate: Date, lastDate: Date): MutableList<SensorData> {
        val lines = data.split("\n").drop(1) // Skip the first line
        val dataTimeDiff = getDataTimeDiff(lines[1], lines[2])
        val stepTimeInSec = getStepTime(firstDate, lastDate)

        val lineStep = (stepTimeInSec / dataTimeDiff).toInt()
        // parsing
        return dataParsing(lines, lineStep)
    }

    private fun getDataTimeDiff(str1: String, str2: String): Long {
        val time1 = getTime(str1)
        val time2 = getTime(str2)
        return time2 - time1
    }

    private fun getTime(line: String): Long {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val parts = line.split(", ")
        val dateString = parts[0]
        val date = dateFormat.parse(dateString) ?: return 0 // Parsing failed, return 0
        return date.time / 1000
    }

    private fun getStepTime(date1: Date, date2: Date): Int {
        val timeDiffInMillis = date2.time - date1.time
        val timeDiffInSecs = timeDiffInMillis / 1000

        // if diff is bigger then a day i want a value every 30 min else every 5 min
        return if (timeDiffInSecs <= (24 * 60 * 60)) (5 * 60) else (30 * 60)
    }


    private fun dataParsing(lines: List<String>, lineStep: Int): MutableList<SensorData> {
        val sensorDataList = mutableListOf<SensorData>()
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())

        for ((counter, line) in lines.withIndex()) {
            if (counter == 0 || counter % lineStep == 0) {
                val parts = line.split(", ")
                val dateString = parts[0]
                val temp = parts[1].toDouble()
                val hum = parts[2].toDouble()

                val date = try {
                    dateFormat.parse(dateString)
                } catch (e: ParseException) {
                    Date(0) // If parsing fails, use the default date
                }
                val sensorData = SensorData(date, temp, hum)
                sensorDataList.add(sensorData)
            }
        }
        return sensorDataList
    }

    fun getMaxMinTempHum(data: String): MaxMins {
        val lines = data.split("\n").drop(1) // Skip the first line
        var maxTemp = Double.MIN_VALUE
        var minTemp = Double.MAX_VALUE
        var maxHum = Double.MIN_VALUE
        var minHum = Double.MAX_VALUE

        for (line in lines) {
            val values = line.split(", ")
            if (values.size >= 3) {
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

}