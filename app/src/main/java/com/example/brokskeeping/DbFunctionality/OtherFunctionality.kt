package com.example.brokskeeping.DbFunctionality

import java.time.Year

object OtherFunctionality {

    fun yearlyReset(dbHelper: DatabaseHelper) {
        val query = "UPDATE ${DatabaseHelper.TABLE_HIVES} SET ${DatabaseHelper.COL_HIVE_WINTER_READY} = 0"

        try {
            val db = dbHelper.writableDatabase
            db.execSQL(query)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}