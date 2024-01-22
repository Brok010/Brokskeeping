package com.example.brokskeeping

import android.content.Context
import androidx.appcompat.app.AlertDialog

object Utils {
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

}