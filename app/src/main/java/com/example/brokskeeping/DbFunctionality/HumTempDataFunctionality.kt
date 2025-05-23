package com.example.brokskeeping.DbFunctionality

import android.content.ContentValues
import android.database.Cursor
import com.example.brokskeeping.DataClasses.HumTempData
import java.util.Date

object HumTempDataFunctionality {

    fun getHumTempData(dbHelper: DatabaseHelper, logId: Int): HumTempData {
        val db = dbHelper.readableDatabase
        var humTempData = HumTempData()

        val query = "SELECT * FROM ${DatabaseHelper.TABLE_DATA_LOGS} WHERE " +
                "${DatabaseHelper.COL_DATA_LOG_ID} = ?"
        val selectionArgs = arrayOf(logId.toString())

        db.rawQuery(query, selectionArgs).use { cursor ->
            if (cursor.moveToFirst()) {
                humTempData = getLogFromCursor(cursor)
            }
        }
        return humTempData
    }

    fun getAllHumTempData(dbHelper: DatabaseHelper, hiveId: Int): MutableList<HumTempData> {
        val humTempList = mutableListOf<HumTempData>()
        val db = dbHelper.readableDatabase

        val query = "SELECT * FROM ${DatabaseHelper.TABLE_DATA_LOGS} WHERE " +
                "${DatabaseHelper.COL_HIVE_ID_FK_DATA_LOGS} = ?"
        val selectionArgs = arrayOf(hiveId.toString())

        val cursor = db.rawQuery(query, selectionArgs)
        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                val humTempData = getLogFromCursor(cursor)
                humTempList.add(humTempData)
            }
        }
        return humTempList
    }
    private fun getLogFromCursor(cursor: Cursor): HumTempData {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATA_LOG_ID))
        val hiveId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_ID_FK_DATA_LOGS))
        val stationId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATION_ID_FK_DATA_LOGS))
        val logText = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATA_LOG_DATA))
        val firstDate = Date(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATA_LOG_FIRST_DATE)))
        val lastDate = Date(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATA_LOG_LAST_DATE)))

        return HumTempData(id, stationId, hiveId, logText, firstDate, lastDate)
    }

    fun deleteHivesHumTempData(dbHelper: DatabaseHelper, hiveId: Int): Int {
        val db = dbHelper.writableDatabase
        val deletedRows = db.delete(
            DatabaseHelper.TABLE_DATA_LOGS,
            "${DatabaseHelper.COL_HIVE_ID_FK_DATA_LOGS} = ?",
            arrayOf(hiveId.toString())
        )
        db.close()
        return if (deletedRows > 0) 1 else 0
    }

    fun deleteHumTempData(dbHelper: DatabaseHelper, logId: Int) {
        val db = dbHelper.writableDatabase

        val selection = "${DatabaseHelper.COL_DATA_LOG_ID} = ?"
        val selectionArgs = arrayOf(logId.toString())

        db.delete(DatabaseHelper.TABLE_DATA_LOGS, selection, selectionArgs)

        db.close()
    }

    fun addDataLogs(dbHelper: DatabaseHelper, data: HumTempData) {
        val db = dbHelper.writableDatabase

        // Prepare the data to be inserted
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_STATION_ID_FK_DATA_LOGS, data.stationId)
            put(DatabaseHelper.COL_HIVE_ID_FK_DATA_LOGS, data.hiveId)
            put(DatabaseHelper.COL_DATA_LOG_DATA, data.logText)
            put(DatabaseHelper.COL_DATA_LOG_FIRST_DATE, data.firstDate.time)
            put(DatabaseHelper.COL_DATA_LOG_LAST_DATE, data.lastDate.time)
        }

        // Insert the data into the DATA_LOGS table
        db.insert(DatabaseHelper.TABLE_DATA_LOGS, null, values)
        db.close()
    }
}