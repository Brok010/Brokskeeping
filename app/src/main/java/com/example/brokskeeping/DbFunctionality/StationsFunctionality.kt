package com.example.brokskeeping.DbFunctionality

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.example.brokskeeping.DataClasses.Station
import com.example.brokskeeping.Functionality.Utils
import java.time.LocalDateTime
import java.util.Date

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

    fun getHiveCount(dbHelper: DatabaseHelper, stationId: Int): Pair<Int, Int> {
        // SQL query to count hives with COL_HIVE_DEATH == 0 and matching stationId
        val query = """
        SELECT COUNT(*) 
        FROM ${DatabaseHelper.TABLE_HIVES} 
        WHERE ${DatabaseHelper.COL_STATION_ID_FK_HIVES} = ? 
        AND ${DatabaseHelper.COL_HIVE_COLONY_END_STATE} = -1
    """
        val selectionArgs = arrayOf(stationId.toString())

        return try {
            val cursor = dbHelper.readableDatabase.rawQuery(query, selectionArgs)
            cursor.use {
                if (it.moveToFirst()) {
                    val count = it.getInt(0) // COUNT(*) is always the first column
                    Pair(count, 1) // Return count and 1 (success)
                } else {
                    Pair(0, 1) // No hives found, but still success (just no records)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(0, 0) // Return count 0 and 0 (failure) if an error occurs
        }
    }



    fun saveStation(dbHelper: DatabaseHelper, newStation: Station, startingHives: Int? = null) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        var stationId = newStation.id

        try {
            val stationValues = ContentValues().apply {
                put(DatabaseHelper.COL_STATION_NAME, newStation.name)
                put(DatabaseHelper.COL_STATION_PLACE, newStation.location)
                put(DatabaseHelper.COL_STATION_IN_USE, newStation.inUse)
                put(DatabaseHelper.COL_STATION_CREATION_TIME, newStation.creationTime.time)
                put(DatabaseHelper.COL_STATION_DEATH_TIME, newStation.deathTime.time)
            }

            if (stationId > 0) {
                // Update existing station
                val rowsUpdated = db.update(
                    DatabaseHelper.TABLE_STATIONS,
                    stationValues,
                    "${DatabaseHelper.COL_STATION_ID} = ?",
                    arrayOf(stationId.toString())
                )

                if (rowsUpdated == 0) {
                    Log.e("DatabaseHelper", "Failed to update station with ID $stationId")
                } else {
                    db.setTransactionSuccessful()
                }
            } else {
                // Insert new station
                stationId = db.insert(DatabaseHelper.TABLE_STATIONS, null, stationValues).toInt()
                newStation.id = stationId

                if (stationId != -1) {
                    db.setTransactionSuccessful()
                } else {
                    Log.e("DatabaseHelper", "Failed to insert station")
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
            db.close()

            if (startingHives != null && stationId != -1) {
                createHivesForStation(dbHelper, newStation, startingHives)
            }
        }
    }


    fun createHivesForStation(dbHelper: DatabaseHelper, newStation: Station, hiveCount: Int) {
        val newStationName = newStation.name
        val currentTime = Date()

        for (i in 1..hiveCount) {
            val (orderNum, result) = HivesFunctionality.getNextAvailableHiveStationOrder(dbHelper, newStation.id)
            if (result == 0) {
                Log.e("StationsFunctionality", "getNextAvailableHiveStationOrder failed")
                continue
            }

            val hiveValues = ContentValues().apply {
                put(DatabaseHelper.COL_HIVE_NAME_TAG, "Hive $i for Station $newStationName")
                put(DatabaseHelper.COL_STATION_ID_FK_HIVES, newStation.id)
                put(DatabaseHelper.COL_HIVE_COLONY_END_STATE, -1)
                put(DatabaseHelper.COL_HIVE_CREATION_TIME, currentTime.time)
                put(DatabaseHelper.COL_HIVE_DEATH_TIME, 0)
                put(DatabaseHelper.COL_HIVE_STATION_ORDER, orderNum)
            }

            val db = dbHelper.writableDatabase
            try {
                db.insert(DatabaseHelper.TABLE_HIVES, null, hiveValues)
            } catch (e: Exception) {
                Log.e("StationsFunctionality", "Error inserting hive: ${e.message}")
            } finally {
                db.close()
            }
        }
    }

    fun getAllStations(
        dbHelper: DatabaseHelper,
        inUse: Int? = null,
        creationYear: Int? = null,
        creationMonth: Int? = null,
        deathYear: Int? = null,
        deathMonth: Int? = null
    ): Pair<List<Station>, Int> {
        val stations = mutableListOf<Station>()

        // Get time ranges
        val (creationTimes, creationTimesResult) = Utils.getStartAndEndTime(creationYear, creationMonth)
        if (creationTimesResult == 0) return emptyList<Station>() to 0
        val (startCreationTime, endCreationTime) = creationTimes

        val (deathTimes, deathTimesResult) = Utils.getStartAndEndTime(deathYear, deathMonth)
        if (deathTimesResult == 0) return emptyList<Station>() to 0
        val (startDeathTime, endDeathTime) = deathTimes

        return try {
            val selectionArgs = mutableListOf<String>()
            val whereClauses = mutableListOf<String>()

            if (inUse == 0 || inUse == 1) {
                whereClauses.add("${DatabaseHelper.COL_STATION_IN_USE} = ?")
                selectionArgs.add(inUse.toString())
            }

            // Add creation time filter
            whereClauses.add("${DatabaseHelper.COL_STATION_CREATION_TIME} BETWEEN ? AND ?")
            selectionArgs.add(startCreationTime.toString())
            selectionArgs.add(endCreationTime.toString())

            // Add death time filter
            whereClauses.add("${DatabaseHelper.COL_STATION_DEATH_TIME} BETWEEN ? AND ?")
            selectionArgs.add(startDeathTime.toString())
            selectionArgs.add(endDeathTime.toString())

            val whereClause = if (whereClauses.isNotEmpty()) {
                "WHERE " + whereClauses.joinToString(" AND ")
            } else ""

            val query = "SELECT * FROM ${DatabaseHelper.TABLE_STATIONS} $whereClause"
            val cursor = dbHelper.readableDatabase.rawQuery(query, selectionArgs.toTypedArray())

            cursor.use {
                while (it.moveToNext()) {
                    val station = createStationFromCursor(it)
                    stations.add(station)
                }
            }

            Pair(stations, 1) // Success
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(emptyList(), 0) // Failure
        }
    }


    private fun createStationFromCursor(cursor: Cursor): Station {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATION_ID))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATION_NAME))
        val location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATION_PLACE))
        val inUse = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATION_IN_USE))
        val creationTime = Date(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATION_CREATION_TIME)))
        val deathTime = Date(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATION_DEATH_TIME)))

        return Station(id, name, location, inUse, creationTime, deathTime)
    }

    fun getStationsAttributes(dbHelper: DatabaseHelper, stationId: Int): Pair<Station?, Int> {
        val query = "SELECT * FROM ${DatabaseHelper.TABLE_STATIONS} WHERE ${DatabaseHelper.COL_STATION_ID} = ?"
        val selectionArgs = arrayOf(stationId.toString())
        val cursor = dbHelper.readableDatabase.rawQuery(query, selectionArgs)

        return cursor.use {
            if (it.moveToFirst()) {
                val station = createStationFromCursor(it)
                Pair(station, 1)
            } else {
                Pair(null, 0)
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