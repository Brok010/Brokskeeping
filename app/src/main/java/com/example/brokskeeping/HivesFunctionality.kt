package com.example.brokskeeping

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.database.Cursor
import android.provider.ContactsContract.CommonDataKinds.Note
import android.util.Log
import com.example.brokskeeping.Classes.Beehive
import com.example.brokskeeping.Classes.HiveNotes
import com.example.brokskeeping.Classes.HumTempData

object HivesFunctionality {

    fun adjustHive(dbHelper: DatabaseHelper, stationId: Int, hiveId: Int, hiveName: String) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()

        try {
            val values = ContentValues().apply {
                put(DatabaseHelper.COL_HIVE_NAME_TAG, hiveName)
            }

            val rowsUpdated = db.update(
                DatabaseHelper.TABLE_HIVES, values, "${DatabaseHelper.COL_HIVE_ID} = ?",
                arrayOf(hiveId.toString()))

            if (rowsUpdated > 0) {
                db.setTransactionSuccessful()
            } else {
                Log.e("DatabaseHelper", "Failed to update hive with ID $hiveId")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    fun getAllHives(dbHelper: DatabaseHelper, stationId: Int): List<Beehive> {
        val hives = mutableListOf<Beehive>()

        val query = "SELECT * FROM ${DatabaseHelper.TABLE_HIVES} WHERE ${DatabaseHelper.COL_STATION_ID_FK} = ?"
        val selectionArgs = arrayOf(stationId.toString())

        val cursor = dbHelper.readableDatabase.rawQuery(query, selectionArgs)

        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                val hive = createBeehiveFromCursor(cursor)
                hives.add(hive)
            }
        }
        return hives
    }

    //TODO: prob need to get all notes and datalogs from the database and pass them along?
    private fun createBeehiveFromCursor(cursor: Cursor): Beehive {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_ID))
        val stationId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATION_ID_FK))
        val nameTag = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_NAME_TAG))
        return Beehive(id, stationId, nameTag)
    }

    fun saveHive(dbHelper: DatabaseHelper, currentStationId: Int, nameTag: String, currentNotes: String, fileData: String) {
        val db = dbHelper.writableDatabase

        // Prepare the data to be inserted
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_STATION_ID_FK, currentStationId)
            put(DatabaseHelper.COL_HIVE_NAME_TAG, nameTag)
        }

        // Insert the data into the Hives table
        val currentHiveId = db.insert(DatabaseHelper.TABLE_HIVES, null, values).toInt()

        // Check if the insertion was successful
        if (currentHiveId != -1) {
            // If successful, insert additional data into other tables as needed
            if (fileData != "None") {
                val data = HumTempData(stationId = currentStationId, hiveId = currentHiveId, logText = fileData)
                HumTempDataFunctionality.addDataLogs(dbHelper, data)
            }
            if (currentNotes != "None") {
                val data = HiveNotes(stationId = currentStationId, hiveId = currentHiveId, noteText = currentNotes)
                NotesFunctionality.addFirstHiveNote(dbHelper, data)
            }
        }
        db.close()
    }
    fun deleteHive(dbHelper: DatabaseHelper, stationId: Int, hiveId: Int) {
        val db = dbHelper.writableDatabase
        NotesFunctionality.deleteNotes(dbHelper, hiveId)
        HumTempDataFunctionality.deleteHivesHumTempData(dbHelper, hiveId)
        db.beginTransaction()

        try {
            // Delete the hive
            val rowsDeleted = db.delete(
                DatabaseHelper.TABLE_HIVES, "${DatabaseHelper.COL_HIVE_ID} = ? AND ${DatabaseHelper.COL_STATION_ID_FK} = ?",
                arrayOf(hiveId.toString(), stationId.toString()))

            // Check if deletion was successful
            if (rowsDeleted > 0) {
                db.setTransactionSuccessful()
            } else {
                // Handle deletion failure
                Log.e("DatabaseHelper", "Failed to delete hive with ID $hiveId for station with ID $stationId")
            }
        } catch (e: Exception) {
            // Handle any exceptions that may occur during the transaction
            e.printStackTrace()
        } finally {
            // End the transaction
            db.endTransaction()
        }
        //subtract a hive from the sum
        StationsFunctionality.updateHiveNumber(dbHelper, stationId, -1)
    }
}