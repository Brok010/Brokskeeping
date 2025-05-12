package com.example.brokskeeping.DbFunctionality

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.example.brokskeeping.DataClasses.Station

object StationsFunctionality {

    fun GetHiveIdsOfStation(dbHelper: DatabaseHelper, stationId: Int): List<Int> {
        val hiveIds = mutableListOf<Int>()
        val db = dbHelper.readableDatabase

        // SQL query to get hive IDs associated with a specific station
        val query = "SELECT ${DatabaseHelper.COL_HIVE_ID} FROM ${DatabaseHelper.TABLE_HIVES} WHERE ${DatabaseHelper.COL_STATION_ID_FK_HIVES} = ?"
        val cursor = db.rawQuery(query, arrayOf(stationId.toString()))

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val hiveId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_ID))
                hiveIds.add(hiveId)
            }
            cursor.close()
        }

        db.close()
        return hiveIds
    }

    fun getStationNameById(dbHelper: DatabaseHelper, stationId: Int): String {
        var stationName: String = ""
        val db = dbHelper.readableDatabase

        // SQL query to get the station name by its ID
        val query = "SELECT ${DatabaseHelper.COL_STATION_NAME} FROM ${DatabaseHelper.TABLE_STATIONS} WHERE ${DatabaseHelper.COL_STATION_ID} = ?"
        val cursor = db.rawQuery(query, arrayOf(stationId.toString()))

        if (cursor != null) {
            // Move to the first row in the cursor
            if (cursor.moveToFirst()) {
                // Get the station name from the first column
                stationName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATION_NAME))
            }
            // Close the cursor after use
            cursor.close()
        }

        // Close the database connection
        db.close()
        return stationName
    }

    fun getHiveCount(dbHelper: DatabaseHelper, stationId: Int): Int {
        val query = "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_HIVES} WHERE ${DatabaseHelper.COL_STATION_ID_FK_HIVES} = ?"
        val selectionArgs = arrayOf(stationId.toString())

        val cursor = dbHelper.readableDatabase.rawQuery(query, selectionArgs)

        return try {
            if (cursor.moveToFirst()) {
                cursor.getInt(0) // COUNT(*) is always the first column
            } else {
                0
            }
        } finally {
            cursor.close()
        }
    }

    fun saveStation(dbHelper: DatabaseHelper, newStation: Station, startingHives: Int) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        var stationId = -1

        try {
            val stationValues = ContentValues().apply {
                put(DatabaseHelper.COL_STATION_NAME, newStation.name)
                put(DatabaseHelper.COL_STATION_PLACE, newStation.location)
                put(DatabaseHelper.COL_STATION_IN_USE, newStation.inUse)
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
            createHivesForStation(dbHelper, stationId, startingHives)
        }
    }

    fun createHivesForStation(dbHelper: DatabaseHelper, stationId: Int, hiveCount: Int) {
        val db = dbHelper.writableDatabase

        for (i in 1..hiveCount) {
            val hiveValues = ContentValues().apply {
                put(DatabaseHelper.COL_HIVE_NAME_TAG, "Hive $i for Station $stationId")
                put(DatabaseHelper.COL_STATION_ID_FK_HIVES, stationId)
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
        val inUse = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATION_IN_USE))

        return Station(id, name, location, inUse)
    }

    fun getStationsAttributes(dbHelper: DatabaseHelper, stationId: Int): Station? {
        val query = "SELECT * FROM ${DatabaseHelper.TABLE_STATIONS} WHERE ${DatabaseHelper.COL_STATION_ID} = ?"
        val selectionArgs = arrayOf(stationId.toString())
        val cursor = dbHelper.readableDatabase.rawQuery(query, selectionArgs)

        return cursor.use { cursor ->
            if (cursor.moveToFirst()) {
                createStationFromCursor(cursor)
            } else {
                null
            }
        }
    }

    fun getStationIdByName(dbHelper: DatabaseHelper, stationName: String): Int {
        var stationId = -1
        val db = dbHelper.readableDatabase

        // SQL query to get the station ID by name
        val query = "SELECT ${DatabaseHelper.COL_STATION_ID} FROM ${DatabaseHelper.TABLE_STATIONS} WHERE ${DatabaseHelper.COL_STATION_NAME} = ?"
        val cursor = db.rawQuery(query, arrayOf(stationName))

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                stationId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATION_ID))
            }
            cursor.close()
        }

        db.close()
        return stationId
    }


    fun adjustStation(dbHelper: DatabaseHelper, stationId: Int, updatedStation: Station) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()

        try {
            val values = ContentValues().apply {
                put(DatabaseHelper.COL_STATION_NAME, updatedStation.name)
                put(DatabaseHelper.COL_STATION_PLACE, updatedStation.location)
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
            db.delete(DatabaseHelper.TABLE_HIVES, "${DatabaseHelper.COL_STATION_ID_FK_HIVES} = ?", arrayOf(stationId.toString()))

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
}