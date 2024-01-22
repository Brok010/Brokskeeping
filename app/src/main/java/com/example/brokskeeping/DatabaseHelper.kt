package com.example.brokskeeping

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.brokskeeping.Classes.Beehive
import com.example.brokskeeping.Classes.Station

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val CREATE_TABLE_STATIONS = "CREATE TABLE $TABLE_STATIONS (" +
            "$COL_STATION_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COL_STATION_NAME TEXT, " +
            "$COL_STATION_PLACE TEXT, " +
            "$COL_BEEHIVE_NUM INTEGER)"

    private val CREATE_TABLE_HIVES = "CREATE TABLE $TABLE_HIVES (" +
            "$COL_HIVE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COL_HIVE_NOTES TEXT, " +
            "$COL_STATION_ID_FK INTEGER, " +
            "FOREIGN KEY($COL_STATION_ID_FK) REFERENCES $TABLE_STATIONS($COL_STATION_ID))"

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE_STATIONS)
        db?.execSQL(CREATE_TABLE_HIVES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades by dropping and recreating tables
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_STATIONS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_HIVES")

        // Recreate tables with updated schema
        onCreate(db)
    }
    fun getAllHives(stationId: Int): List<Beehive> {
        val hives = mutableListOf<Beehive>()

        val query = "SELECT * FROM $TABLE_HIVES WHERE $COL_STATION_ID_FK = ?"
        val selectionArgs = arrayOf(stationId.toString())

        val cursor = readableDatabase.rawQuery(query, selectionArgs)

        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                val hive = createBeehiveFromCursor(cursor)
                hives.add(hive)
            }
        }
        return hives
    }

    private fun createBeehiveFromCursor(cursor: Cursor): Beehive {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_HIVE_ID))
        val notes = cursor.getString(cursor.getColumnIndexOrThrow(COL_HIVE_NOTES))

        return Beehive(id, notes)
    }

    fun getAllStations(): List<Station> {
        val stations = mutableListOf<Station>()
        val query = "SELECT * FROM $TABLE_STATIONS"
        val cursor = readableDatabase.rawQuery(query, null)

        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                val station = createStationFromCursor(cursor)
                stations.add(station)
            }
        }
        return stations
    }

    fun deleteStation(stationId: Int) {
        val db = writableDatabase
        db.beginTransaction()

        try {
            // Delete hives associated with the station
            db.delete(TABLE_HIVES, "$COL_STATION_ID_FK = ?", arrayOf(stationId.toString()))

            // Delete the station
            val rowsDeleted = db.delete(TABLE_STATIONS, "$COL_STATION_ID = ?", arrayOf(stationId.toString()))

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

    fun deleteHive(stationId: Int, hiveId: Int) {
        val db = writableDatabase
        db.beginTransaction()

        try {
            // Delete the hive
            val rowsDeleted = db.delete(TABLE_HIVES, "$COL_HIVE_ID = ? AND $COL_STATION_ID_FK = ?",
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
        updateHiveNumber(stationId, -1)
    }

    fun getStationsAttributes(stationId: Int): Station? {
        val query = "SELECT * FROM $TABLE_STATIONS WHERE $COL_STATION_ID = ?"
        val selectionArgs = arrayOf(stationId.toString())

        val cursor = readableDatabase.rawQuery(query, selectionArgs)

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

    fun updateStation(stationId: Int, updatedStation: Station) {
        val db = writableDatabase
        db.beginTransaction()

        try {
            val values = ContentValues().apply {
                put(COL_STATION_NAME, updatedStation.name)
                put(COL_STATION_PLACE, updatedStation.location)
                put(COL_BEEHIVE_NUM, updatedStation.beehiveNum)
            }

            val rowsUpdated = db.update(TABLE_STATIONS, values, "$COL_STATION_ID = ?",
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

    fun updateHiveNumber(stationId: Int, number: Int) {
        val db = writableDatabase
        db.beginTransaction()

        try {
            // Get the current hive count for the station
            val currentHiveCount = getHiveCount(stationId)

            // Calculate the new hive count
            val newHiveCount = currentHiveCount + number

            // Update the hive count in the tbl_stations table
            val values = ContentValues().apply {
                put(COL_BEEHIVE_NUM, newHiveCount)
            }

            val rowsUpdated = db.update(TABLE_STATIONS, values, "$COL_STATION_ID = ?",
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


    fun saveStation(newStation: Station) {
        val db = writableDatabase
        db.beginTransaction()
        var stationId = -1

        try {
            // Insert station details into tbl_stations
            val stationValues = ContentValues().apply {
                put(COL_STATION_NAME, newStation.name)
                put(COL_STATION_PLACE, newStation.location)
                put(COL_BEEHIVE_NUM, newStation.beehiveNum)
            }
            stationId = db.insert(TABLE_STATIONS, null, stationValues).toInt()

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
            createHivesForStation(stationId, newStation.beehiveNum)
        }
    }

    fun createHivesForStation(stationId: Int, hiveCount: Int) {
        val db = writableDatabase

        for (i in 1..hiveCount) {
            val hiveValues = ContentValues().apply {
                put(COL_HIVE_NOTES, "Hive $i for Station $stationId")
                put(COL_STATION_ID_FK, stationId)
            }
            db.insert(TABLE_HIVES, null, hiveValues)
        }
        db.close()
    }
    private fun createStationFromCursor(cursor: Cursor): Station {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_STATION_ID))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATION_NAME))
        val location = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATION_PLACE))
        val beehiveNumber = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BEEHIVE_NUM))

        return Station(id, name, location, beehiveNumber)
    }

    fun getHiveCount(stationId: Int): Int {
        val query = "SELECT $COL_BEEHIVE_NUM FROM $TABLE_STATIONS WHERE $COL_STATION_ID = ?"
        val selectionArgs = arrayOf(stationId.toString())

        val cursor = readableDatabase.rawQuery(query, selectionArgs)

        return try {
            if (cursor.moveToFirst()) {
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_BEEHIVE_NUM))
            } else {
                0 // default value
            }
        } finally {
            cursor.close()
        }
    }


    companion object{
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "beehives.db"

        private const val TABLE_STATIONS = "tbl_stations"
        private const val COL_STATION_ID = "stations_id"
        private const val COL_STATION_NAME = "stations_name"
        private const val COL_STATION_PLACE = "stations_place"
        private const val COL_BEEHIVE_NUM = "beehive_number"

        private const val TABLE_HIVES = "tbl_hives"
        private const val COL_HIVE_ID = "hive_id"
        private const val COL_HIVE_NOTES = "hive_notes"
        private const val COL_STATION_ID_FK = "stations_foreign_key"

    }
}