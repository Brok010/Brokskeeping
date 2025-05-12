package com.example.brokskeeping.DbFunctionality

import android.content.ContentValues
import android.database.Cursor
import com.example.brokskeeping.DataClasses.History
import java.util.Date

object HistoryFunctionality {
    fun saveHistoryEvent(dbHelper: DatabaseHelper, history: History): Int {
        return try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(DatabaseHelper.COL_HISTORY_ENTITY_ID, history.entityId)
                put(DatabaseHelper.COL_HISTORY_ENTITY_TYPE, history.entityType)
                put(DatabaseHelper.COL_HISTORY_DATE, history.date.time) // Store as UNIX timestamp
                put(DatabaseHelper.COL_HISTORY_EVENT, history.event)
            }

            val result = db.insert(DatabaseHelper.TABLE_HISTORY, null, values)
            if (result == -1L) 0 else 1
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    private fun createHistoryFromCursor(cursor: Cursor): History {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HISTORY_ID))
        val entityId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HISTORY_ENTITY_ID))
        val entityType = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HISTORY_ENTITY_TYPE))
        val dateLong = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HISTORY_DATE))
        val event = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HISTORY_EVENT))

        return History(
            id = id,
            entityId = entityId,
            entityType = entityType,
            date = Date(dateLong),
            event = event
        )
    }

    fun getHistoryById(dbHelper: DatabaseHelper, id: Int): Pair<History?, Int> {
        var history: History? = null
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                DatabaseHelper.TABLE_HISTORY,
                null,
                "${DatabaseHelper.COL_HISTORY_ID} = ?",
                arrayOf(id.toString()),
                null, null, null
            )

            if (cursor.moveToFirst()) {
                history = createHistoryFromCursor(cursor)
                cursor.close()
                Pair(history, 1)
            } else {
                cursor.close()
                Pair(null, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(null, 0)
        }
    }

    fun deleteHistory(dbHelper: DatabaseHelper, id: Int): Int {
        return try {
            val db = dbHelper.writableDatabase
            val result = db.delete(
                DatabaseHelper.TABLE_HISTORY,
                "${DatabaseHelper.COL_HISTORY_ID} = ?",
                arrayOf(id.toString())
            )
            if (result > 0) 1 else 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    fun getAllHistory(
        dbHelper: DatabaseHelper,
        entityId: Int,
        entityType: String,
        dateMin: Int = -1,
        dateMax: Int = -1
    ): Pair<List<History>, Int> {
        val historyList = mutableListOf<History>()

        return try {
            val db = dbHelper.readableDatabase

            val selectionBuilder = StringBuilder()
            val selectionArgsList = mutableListOf<String>()

            selectionBuilder.append("${DatabaseHelper.COL_HISTORY_ENTITY_ID} = ? AND ${DatabaseHelper.COL_HISTORY_ENTITY_TYPE} = ?")
            selectionArgsList.add(entityId.toString())
            selectionArgsList.add(entityType)

            if (dateMin > -1) {
                selectionBuilder.append(" AND ${DatabaseHelper.COL_HISTORY_DATE} >= ?")
                selectionArgsList.add(dateMin.toString())
            }

            if (dateMax > -1) {
                selectionBuilder.append(" AND ${DatabaseHelper.COL_HISTORY_DATE} <= ?")
                selectionArgsList.add(dateMax.toString())
            }

            val cursor = db.query(
                DatabaseHelper.TABLE_HISTORY,
                null,
                selectionBuilder.toString(),
                selectionArgsList.toTypedArray(),
                null,
                null,
                "${DatabaseHelper.COL_HISTORY_DATE} DESC"
            )

            if (cursor.moveToFirst()) {
                do {
                    val history = createHistoryFromCursor(cursor)
                    historyList.add(history)
                } while (cursor.moveToNext())
            }
            cursor.close()
            Pair(historyList, 1)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(emptyList(), 0)
        }
    }
}