package com.example.brokskeeping.DbFunctionality

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.example.brokskeeping.DataClasses.Station

object StationsFunctionality {

    fun getHiveCount(dbHelper: DatabaseHelper, stationId: Int): Int {
        val query = "SELECT ${DatabaseHelper.COL_BEEHIVE_NUM} FROM ${DatabaseHelper.TABLE_STATIONS} WHERE ${DatabaseHelper.COL_STATION_ID} = ?"
        val selectionArgs = arrayOf(stationId.toString())

        val cursor = dbHelper.readableDatabase.rawQuery(query, selectionArgs)

        return try {
            if (cursor.moveToFirst()) {
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BEEHIVE_NUM))
            } else {
                0 // default value
            }
        } finally {
            cursor.close()
        }
    }
    fun saveStation(dbHelper: DatabaseHelper, newStation: Station) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        var stationId = -1

        try {
            // Insert station details into tbl_stations
            val stationValues = ContentValues().apply {
                put(DatabaseHelper.COL_STATION_NAME, newStation.name)
                put(DatabaseHelper.COL_STATION_PLACE, newStation.location)
                put(DatabaseHelper.COL_BEEHIVE_NUM, newStation.beehiveNum)
            }
            stationId = db.insert(DatabaseHelper.TABLE_STATIONS, null, stationValues).toInt()

            // Check if station insertion was successful
            if (stationId != -1) {
                // Set the transaction as successful
                db.setTransactionSuccessful()

            } else {
                // Handle station insertion failure
                Log.e("DatabaseHelper", "Failed to insert station into tbl_stations")
            }
        } catch (e: Exception) {
            // Handle any exceptions that may occur during the transaction
            e.printStackTrace()
        } finally {
            // End the transaction
            db.endTransaction()

            // Close the database
            db.close()

            // Insert hives into tbl_hives based on beehiveNumber
            createHivesForStation(dbHelper, stationId, newStation.beehiveNum)
        }
    }

    fun createHivesForStation(dbHelper: DatabaseHelper, stationId: Int, hiveCount: Int) {
        val db = dbHelper.writableDatabase

        for (i in 1..hiveCount) {
            val hiveValues = ContentValues().apply {
                put(DatabaseHelper.COL_HIVE_NAME_TAG, "Hive $i for Station $stationId")
                put(DatabaseHelper.COL_STATION_ID_FK, stationId)
            }
            db.insert(DatabaseHelper.TABLE_HIVES, null, hiveValues)
        }
        db.close()
    }
    fun getAllStations(dbHelper: DatabaseHelper): List<Station> {
        val stations = mutableListOf<Station>()
        val query = "SELECT * FROM ${DatabaseHelper.TABLE_STATIONS}"
        val cursor = dbHelper.readableDatabase.rawQuery(query, null)

        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                val station = createStationFromCursor(cursor)
                stations.add(station)
            }
        }
        return stations
    }

    private fun createStationFromCursor(cursor: Cursor): Station {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATION_ID))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATION_NAME))
        val location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATION_PLACE))
        val beehiveNumber = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BEEHIVE_NUM))

        return Station(id, name, location, beehiveNumber)
    }

    fun getStationsAttributes(dbHelper: DatabaseHelper, stationId: Int): Station? {
        val query = "SELECT * FROM ${DatabaseHelper.TABLE_STATIONS} WHERE ${DatabaseHelper.COL_STATION_ID} = ?"
        val selectionArgs = arrayOf(stationId.toString())

        val cursor = dbHelper.readableDatabase.rawQuery(query, selectionArgs)

        return try {
            if (cursor.moveToFirst()) {
                createStationFromCursor(cursor)
            } else {
                null
            }
        } finally {
            cursor.close()
        }
    }

    fun adjustStation(dbHelper: DatabaseHelper, stationId: Int, updatedStation: Station) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()

        try {
            val values = ContentValues().apply {
                put(DatabaseHelper.COL_STATION_NAME, updatedStation.name)
                put(DatabaseHelper.COL_STATION_PLACE, updatedStation.location)
                put(DatabaseHelper.COL_BEEHIVE_NUM, updatedStation.beehiveNum)
            }

            val rowsUpdated = db.update(
                DatabaseHelper.TABLE_STATIONS, values, "${DatabaseHelper.COL_STATION_ID} = ?",
                arrayOf(stationId.toString()))

            if (rowsUpdated > 0) {
                db.setTransactionSuccessful()
            } else {
                Log.e("DatabaseHelper", "Failed to update station with ID $stationId")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    fun deleteStation(dbHelper: DatabaseHelper, stationId: Int) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()

        try {
            // Delete hives associated with the station
            db.delete(DatabaseHelper.TABLE_HIVES, "${DatabaseHelper.COL_STATION_ID_FK} = ?", arrayOf(stationId.toString()))

            // Delete the station
            val rowsDeleted = db.delete(DatabaseHelper.TABLE_STATIONS, "${DatabaseHelper.COL_STATION_ID} = ?", arrayOf(stationId.toString()))

            // Check if deletion was successful
            if (rowsDeleted > 0) {
                db.setTransactionSuccessful()
            } else {
                // Handle deletion failure
                Log.e("DatabaseHelper", "Failed to delete station with ID $stationId")
            }
        } catch (e: Exception) {
            // Handle any exceptions that may occur during the transaction
            e.printStackTrace()
        } finally {
            // End the transaction
            db.endTransaction()
        }
    }
    fun updateHiveNumber(dbHelper: DatabaseHelper, stationId: Int, number: Int) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()

        try {
            // Get the current hive count for the station
            val currentHiveCount = getHiveCount(dbHelper, stationId)

            // Calculate the new hive count
            val newHiveCount = currentHiveCount + number

            // Update the hive count in the tbl_stations table
            val values = ContentValues().apply {
                put(DatabaseHelper.COL_BEEHIVE_NUM, newHiveCount)
            }

            val rowsUpdated = db.update(
                DatabaseHelper.TABLE_STATIONS, values, "${DatabaseHelper.COL_STATION_ID} = ?",
                arrayOf(stationId.toString()))

            // Check if the update was successful
            if (rowsUpdated > 0) {
                db.setTransactionSuccessful()
            } else {
                // Handle update failure
                Log.e("DatabaseHelper", "Failed to update hive count for station with ID $stationId")
            }
        } catch (e: Exception) {
            // Handle any exceptions that may occur during the transaction
            e.printStackTrace()
        } finally {
            // End the transaction
            db.endTransaction()
        }
    }
}