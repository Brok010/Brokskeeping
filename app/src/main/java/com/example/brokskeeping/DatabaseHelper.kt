package com.example.brokskeeping

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.brokskeeping.Classes.Beehive
import com.example.brokskeeping.Classes.HiveNotes
import com.example.brokskeeping.Classes.HumTempData
import com.example.brokskeeping.Classes.Station
import java.util.Date

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val CREATE_TABLE_STATIONS = "CREATE TABLE $TABLE_STATIONS (" +
            "$COL_STATION_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COL_STATION_NAME TEXT, " +
            "$COL_STATION_PLACE TEXT, " +
            "$COL_BEEHIVE_NUM INTEGER)"

    private val CREATE_TABLE_HIVES = "CREATE TABLE $TABLE_HIVES (" +
            "$COL_HIVE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COL_STATION_ID_FK INTEGER, " +
            "$COL_HIVE_NAME_TAG TEXT, " +
            "FOREIGN KEY($COL_STATION_ID_FK) REFERENCES $TABLE_STATIONS($COL_STATION_ID))"

    private val CREATE_TABLE_DATA_LOGS = "CREATE TABLE $TABLE_DATA_LOGS (" +
            "$COL_DATA_LOG_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COL_STATION_ID_FK_DATA_LOGS INTEGER, " +
            "$COL_HIVE_ID_FK_DATA_LOGS INTEGER, " +
            "$COL_DATA_LOG_DATA TEXT, " +
            "$COL_DATA_LOG_FIRST_DATE INTEGER, " + //need to store them as unix
            "$COL_DATA_LOG_LAST_DATE INTEGER, " +
            "FOREIGN KEY($COL_STATION_ID_FK_DATA_LOGS) REFERENCES $TABLE_STATIONS($COL_STATION_ID), " +
            "FOREIGN KEY($COL_HIVE_ID_FK_DATA_LOGS) REFERENCES $TABLE_HIVES($COL_HIVE_ID))"

    private val CREATE_TABLE_HIVE_NOTES = "CREATE TABLE $TABLE_HIVE_NOTES (" +
            "$COL_HIVE_NOTE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COL_STATION_ID_FK_HIVE_NOTES INTEGER, " +
            "$COL_HIVE_ID_FK_HIVE_NOTES INTEGER, " +
            "$COL_HIVE_NOTE TEXT, " +
            "$COL_HIVE_NOTE_DATE INTEGER, " +
            "FOREIGN KEY($COL_STATION_ID_FK_HIVE_NOTES) REFERENCES $TABLE_STATIONS($COL_STATION_ID), " +
            "FOREIGN KEY($COL_HIVE_ID_FK_HIVE_NOTES) REFERENCES $TABLE_HIVES($COL_HIVE_ID))"

    private val CREATE_TABLE_TODO = "CREATE TABLE $TABLE_TODO (" +
            "$COL_TODO_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COL_HIVE_ID_FK_TODO INTEGER, " +
            "$COL_NOTES_ID_FK_TODO INTEGER, " +
            "$COL_TODO_TEXT TEXT, " +
            "$COL_TODO_STATE INTEGER, " + // Store boolean as INTEGER (0 or 1)
            "$COL_TODO_DATE INTEGER," +
            "FOREIGN KEY($COL_HIVE_ID_FK_TODO) REFERENCES $TABLE_HIVES($COL_HIVE_ID)," +
            "FOREIGN KEY($COL_NOTES_ID_FK_TODO) REFERENCES $TABLE_HIVE_NOTES($COL_HIVE_NOTE_ID))"

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE_STATIONS)
        db?.execSQL(CREATE_TABLE_HIVES)
        db?.execSQL(CREATE_TABLE_DATA_LOGS)
        db?.execSQL(CREATE_TABLE_HIVE_NOTES)
        db?.execSQL(CREATE_TABLE_TODO)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades by dropping and recreating tables
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_STATIONS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_HIVES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_DATA_LOGS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_HIVE_NOTES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TODO")


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

    //TODO: prob need to get all notes and datalogs from the database and pass them along?
    private fun createBeehiveFromCursor(cursor: Cursor): Beehive {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_HIVE_ID))
        val stationId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_STATION_ID_FK))
        val nameTag = cursor.getString(cursor.getColumnIndexOrThrow(COL_HIVE_NAME_TAG))
        return Beehive(id, stationId, nameTag)
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
        deleteNotes(hiveId)
        deleteHumTempData(hiveId)
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

    // Function to delete notes associated with a specific hive
    //TODO: test if it works
    fun deleteNotes(hiveId: Int) {
        val db = writableDatabase
        db.delete(TABLE_HIVE_NOTES, "$COL_HIVE_ID_FK_HIVE_NOTES = ?", arrayOf(hiveId.toString()))
        db.close()
    }

    fun getAllNotes(hiveId: Int): MutableList<HiveNotes> {
        val notesList = mutableListOf<HiveNotes>()
        val db = this.readableDatabase

        val selectQuery = "SELECT * FROM $TABLE_HIVE_NOTES WHERE $COL_HIVE_ID_FK_HIVE_NOTES = ?"
        val cursor = db.rawQuery(selectQuery, arrayOf(hiveId.toString()))

        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                val noteId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_HIVE_NOTE_ID))
                val stationId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_STATION_ID_FK_HIVE_NOTES))
                val hiveId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_HIVE_ID_FK_HIVE_NOTES))
                val noteText = cursor.getString(cursor.getColumnIndexOrThrow(COL_HIVE_NOTE))
                //Date
                val noteDateInMillis = cursor.getLong(cursor.getColumnIndexOrThrow(COL_HIVE_NOTE_DATE))
                val noteDate = Date(noteDateInMillis)

                val hiveNote = HiveNotes(noteId, stationId, hiveId, noteText, noteDate)
                notesList.add(hiveNote)
            }
        }
        return notesList
    }

    // Function to delete humidity and temperature data associated with a specific hive
    //TODO: test if it works
    fun deleteHumTempData(hiveId: Int) {
        val db = writableDatabase
        db.delete(TABLE_DATA_LOGS, "$COL_HIVE_ID_FK_DATA_LOGS = ?", arrayOf(hiveId.toString()))
        db.close()
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

    fun saveHive(currentStationId: Int, nameTag: String, currentNotes: String, fileData: String) {
        val db = writableDatabase

        // Prepare the data to be inserted
        val values = ContentValues().apply {
            put(COL_STATION_ID_FK, currentStationId)
            put(COL_HIVE_NAME_TAG, nameTag)
        }

        // Insert the data into the Hives table
        val currentHiveId = db.insert(TABLE_HIVES, null, values).toInt()

        // Check if the insertion was successful
        if (currentHiveId != -1) {
            // If successful, insert additional data into other tables as needed
            if (fileData != "None") {
                val data = HumTempData(stationId = currentStationId, hiveId = currentHiveId, logText = fileData)
                addDataLogs(data)
            }
            if (currentNotes != "None") {
                val data = HiveNotes(stationId = currentStationId, hiveId = currentHiveId, notesText = currentNotes)
                addHiveNotes(data)
            }
        }
        db.close()
    }

    //TODO: prob would be optimal to get first and last dates out the data and insert them too
    fun addDataLogs(data: HumTempData) {
        val db = writableDatabase

        // Prepare the data to be inserted
        val values = ContentValues().apply {
            put(COL_STATION_ID_FK_DATA_LOGS, data.stationId)
            put(COL_HIVE_ID_FK_DATA_LOGS, data.hiveId)
            put(COL_DATA_LOG_DATA, data.logText)
            // Additional columns if needed
        }

        // Insert the data into the DATA_LOGS table
        db.insert(TABLE_DATA_LOGS, null, values)

        db.close()
    }

    fun addHiveNotes(data: HiveNotes) {
        val db = writableDatabase

        // Prepare the data to be inserted
        val values = ContentValues().apply {
            put(COL_STATION_ID_FK_HIVE_NOTES, data.stationId)
            put(COL_HIVE_ID_FK_HIVE_NOTES, data.hiveId)
            put(COL_HIVE_NOTE, data.notesText)

            // Check if the date was set to the default value
            val dateToInsert = if (data.date == Date(0)) {
                Date(System.currentTimeMillis())
            } else {
                data.date
            }

            // Convert Date to Unix timestamp (milliseconds since the epoch)
            put(COL_HIVE_NOTE_DATE, dateToInsert.time)
        }

        // Insert the data into the HIVE_NOTES table
        db.insert(TABLE_HIVE_NOTES, null, values)

        db.close()
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
                put(COL_HIVE_NAME_TAG, "Hive $i for Station $stationId")
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

        //Station
        private const val TABLE_STATIONS = "tbl_stations"
        private const val COL_STATION_ID = "stations_id"
        private const val COL_STATION_NAME = "stations_name"
        private const val COL_STATION_PLACE = "stations_place"
        private const val COL_BEEHIVE_NUM = "beehive_number"

        //Hive
        private const val TABLE_HIVES = "tbl_hives"
        private const val COL_HIVE_ID = "hive_id"
        private const val COL_HIVE_NAME_TAG = "hive_name_tag"
        private const val COL_STATION_ID_FK = "stations_foreign_key"

        //Datalogs
        private const val TABLE_DATA_LOGS = "tbl_data_logs"
        private const val COL_DATA_LOG_ID = "data_log_id"
        private const val COL_STATION_ID_FK_DATA_LOGS = "station_foreign_key_data_logs"
        private const val COL_HIVE_ID_FK_DATA_LOGS = "hive_foreign_key_data_logs"
        private const val COL_DATA_LOG_FIRST_DATE = "data_log_first_date"
        private const val COL_DATA_LOG_LAST_DATE = "data_log_last_date"
        private const val COL_DATA_LOG_DATA = "data_log_data"

        //Hive Notes
        private const val TABLE_HIVE_NOTES = "tbl_hive_notes"
        private const val COL_HIVE_NOTE_ID = "hive_note_id"
        private const val COL_STATION_ID_FK_HIVE_NOTES = "station_foreign_key_note"
        private const val COL_HIVE_ID_FK_HIVE_NOTES = "hive_foreign_key_note"
        private const val COL_HIVE_NOTE = "hive_note"
        private const val COL_HIVE_NOTE_DATE = "hive_note_date"

        //T0DO
        private const val TABLE_TODO = "tbl_todo"
        private const val COL_TODO_ID = "todo_id"
        private const val COL_HIVE_ID_FK_TODO = "todo_hive_id"
        private const val COL_NOTES_ID_FK_TODO = "todo_notes_id"
        private const val COL_TODO_TEXT = "todo_text"
        private const val COL_TODO_STATE = "todo_state"
        private const val COL_TODO_DATE = "todo_date"
    }
}