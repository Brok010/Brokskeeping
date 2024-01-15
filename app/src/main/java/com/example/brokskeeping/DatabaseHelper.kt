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

    fun saveStation(newStation: Station) {
        // Get a writable database
        val db = writableDatabase
        // Start a transaction
        db.beginTransaction()

        try {
            // Insert station details into tbl_stations
            val stationValues = ContentValues().apply {
                put(COL_STATION_NAME, newStation.name)
                put(COL_STATION_PLACE, newStation.location)
                put(COL_BEEHIVE_NUM, newStation.beehiveNum)
            }
            val stationId = db.insert(TABLE_STATIONS, null, stationValues)

            // Check if station insertion was successful
            if (stationId != -1L) {
                // Insert hives into tbl_hives based on beehiveNumber
                for (i in 1..newStation.beehiveNum) {
                    val hiveValues = ContentValues().apply {
                        put(COL_HIVE_NOTES, "Hive $i for Station $stationId")
                        put(COL_STATION_ID_FK, stationId)
                    }
                    db.insert(TABLE_HIVES, null, hiveValues)
                }
                // Set the transaction as successful
                db.setTransactionSuccessful()

            } else {
                // Handle station insertion failure
                // For example, show an error message or log the failure
                Log.e("DatabaseHelper", "Failed to insert station into tbl_stations")
            }
        } catch (e: Exception) {
            // Handle any exceptions that may occur during the transaction
            e.printStackTrace()
        } finally {
            // End the transaction
            db.endTransaction()
        }
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