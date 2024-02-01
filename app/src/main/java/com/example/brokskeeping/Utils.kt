package com.example.brokskeeping

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.brokskeeping.Classes.DateRange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {

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