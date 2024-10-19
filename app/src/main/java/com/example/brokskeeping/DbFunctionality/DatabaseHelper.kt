package com.example.brokskeeping.DbFunctionality

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    internal val CREATE_TABLE_STATIONS = "CREATE TABLE $TABLE_STATIONS (" +
            "$COL_STATION_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COL_STATION_NAME TEXT, " +
            "$COL_STATION_PLACE TEXT, " +
            "$COL_BEEHIVE_NUM INTEGER)"

    internal val CREATE_TABLE_HIVES = "CREATE TABLE $TABLE_HIVES (" +
            "$COL_HIVE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COL_STATION_ID_FK INTEGER, " +
            "$COL_HIVE_NAME_TAG TEXT, " +
            "$COL_HIVE_QR_TAG TEXT, " +
            "FOREIGN KEY($COL_STATION_ID_FK) REFERENCES $TABLE_STATIONS($COL_STATION_ID))"

    internal val CREATE_TABLE_DATA_LOGS = "CREATE TABLE $TABLE_DATA_LOGS (" +
            "$COL_DATA_LOG_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COL_STATION_ID_FK_DATA_LOGS INTEGER, " +
            "$COL_HIVE_ID_FK_DATA_LOGS INTEGER, " +
            "$COL_DATA_LOG_DATA TEXT, " +
            "$COL_DATA_LOG_FIRST_DATE INTEGER, " + //need to store them as unix
            "$COL_DATA_LOG_LAST_DATE INTEGER, " +
            "FOREIGN KEY($COL_STATION_ID_FK_DATA_LOGS) REFERENCES $TABLE_STATIONS($COL_STATION_ID), " +
            "FOREIGN KEY($COL_HIVE_ID_FK_DATA_LOGS) REFERENCES $TABLE_HIVES($COL_HIVE_ID))"

    internal val CREATE_TABLE_HIVE_NOTES = "CREATE TABLE $TABLE_HIVE_NOTES (" +
            "$COL_HIVE_NOTE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COL_STATION_ID_FK_HIVE_NOTES INTEGER, " +
            "$COL_HIVE_ID_FK_HIVE_NOTES INTEGER, " +
            "$COL_HIVE_NOTE_TEXT TEXT, " +
            "$COL_HIVE_NOTE_DATE INTEGER, " +
            "FOREIGN KEY($COL_STATION_ID_FK_HIVE_NOTES) REFERENCES $TABLE_STATIONS($COL_STATION_ID), " +
            "FOREIGN KEY($COL_HIVE_ID_FK_HIVE_NOTES) REFERENCES $TABLE_HIVES($COL_HIVE_ID))"

    internal val CREATE_TABLE_TODO = "CREATE TABLE $TABLE_TODO (" +
            "$COL_TODO_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COL_HIVE_ID_FK_TODO INTEGER, " +
            "$COL_TODO_TEXT TEXT, " +
            "$COL_TODO_STATE INTEGER, " + // Store boolean as INTEGER (0 or 1)
            "$COL_TODO_DATE INTEGER," +
            "FOREIGN KEY($COL_HIVE_ID_FK_TODO) REFERENCES $TABLE_HIVES($COL_HIVE_ID))"

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

    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "beehives.db"

        //Station
        const val TABLE_STATIONS = "tbl_stations"
        const val COL_STATION_ID = "stations_id"
        const val COL_STATION_NAME = "stations_name"
        const val COL_STATION_PLACE = "stations_place"
        const val COL_BEEHIVE_NUM = "beehive_number"

        //Hive
        const val TABLE_HIVES = "tbl_hives"
        const val COL_HIVE_ID = "hive_id"
        const val COL_HIVE_NAME_TAG = "hive_name_tag"
        const val COL_HIVE_QR_TAG = "hive_qr_tag"
        const val COL_STATION_ID_FK = "stations_foreign_key"

        //Datalogs
        const val TABLE_DATA_LOGS = "tbl_data_logs"
        const val COL_DATA_LOG_ID = "data_log_id"
        const val COL_STATION_ID_FK_DATA_LOGS = "station_foreign_key_data_logs"
        const val COL_HIVE_ID_FK_DATA_LOGS = "hive_foreign_key_data_logs"
        const val COL_DATA_LOG_FIRST_DATE = "data_log_first_date"
        const val COL_DATA_LOG_LAST_DATE = "data_log_last_date"
        const val COL_DATA_LOG_DATA = "data_log_data"

        //Hive Notes
        const val TABLE_HIVE_NOTES = "tbl_hive_notes"
        const val COL_HIVE_NOTE_ID = "hive_note_id"
        const val COL_STATION_ID_FK_HIVE_NOTES = "station_foreign_key_note"
        const val COL_HIVE_ID_FK_HIVE_NOTES = "hive_foreign_key_note"
        const val COL_HIVE_NOTE_TEXT = "hive_note"
        const val COL_HIVE_NOTE_DATE = "hive_note_date"

        //T0D0
        const val TABLE_TODO = "tbl_todo"
        const val COL_TODO_ID = "todo_id"
        const val COL_HIVE_ID_FK_TODO = "todo_hive_id"
        const val COL_TODO_TEXT = "todo_text"
        const val COL_TODO_STATE = "todo_state"
        const val COL_TODO_DATE = "todo_date"
    }

}