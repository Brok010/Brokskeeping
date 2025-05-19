package com.example.brokskeeping.DbFunctionality

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION, null) {


    internal val CREATE_TABLE_STATIONS = "CREATE TABLE $TABLE_STATIONS (" +
            "$COL_STATION_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COL_STATION_NAME TEXT, " +
            "$COL_STATION_PLACE TEXT, " +
            "$COL_STATION_IN_USE INTEGER, " +
            "$COL_STATION_CREATION_TIME INTEGER, " +
            "$COL_STATION_DEATH_TIME INTEGER)"

    internal val CREATE_TABLE_HIVES = "CREATE TABLE $TABLE_HIVES (" +
            "$COL_HIVE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COL_STATION_ID_FK_HIVES INTEGER, " +
            "$COL_HIVE_NAME_TAG TEXT, " +
            "$COL_HIVE_QR_TAG TEXT, " +
            "$COL_HIVE_FRAMES_PER_SUPER INTEGER, " +
            "$COL_HIVE_SUPERS INTEGER, " +
            "$COL_HIVE_BROOD_FRAMES INTEGER, " +
            "$COL_HIVE_HONEY_FRAMES INTEGER, " +
            "$COL_HIVE_DRONE_BROOD_FRAMES INTEGER, " +
            "$COL_HIVE_FREE_SPACE_FRAMES INTEGER, " +
            "$COL_HIVE_COLONY_ORIGIN TEXT, " +
            "$COL_HIVE_COLONY_END_STATE INTEGER, " +
            "$COL_HIVE_WINTER_READY INTEGER, " +
            "$COL_HIVE_AGGRESSIVITY INTEGER, " +
            "$COL_HIVE_ATTENTION_WORTH INTEGER, " +
            "$COL_HIVE_CREATION_TIME INTEGER, " +
            "$COL_HIVE_DEATH_TIME INTEGER, " +
            "$COL_HIVE_STATION_ORDER INTEGER, " +
            "FOREIGN KEY($COL_STATION_ID_FK_HIVES) REFERENCES $TABLE_STATIONS($COL_STATION_ID))"


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

    internal val CREATE_TABLE_INSPECTIONS = "CREATE TABLE $TABLE_INSPECTIONS (" +
            "$COL_INSPECTION_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COL_STATION_ID_FK_INSPECTION INTEGER, " +
            "$COL_INSPECTION_DATE INTEGER," +
            "$COL_INSPECTION_DATA_IDS TEXT," +
            "FOREIGN KEY($COL_STATION_ID_FK_INSPECTION) REFERENCES $TABLE_STATIONS($COL_STATION_ID))"

    internal val CREATE_TABLE_INSPECTION_DATA = "CREATE TABLE $TABLE_INSPECTION_DATA (" +
            "$COL_INSPECTION_DATA_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COL_HIVE_ID_FK_INSPECTION_DATA INTEGER, " +
            "$COL_NOTE_ID_FK_INSPECTION_DATA INTEGER, " +
            "$COL_INSPECTION_ID_FK_INSPECTION_DATA INTEGER, " +
            "$COL_INSPECTION_DATA_FRAMES_PER_SUPER INTEGER, " +
            "$COL_INSPECTION_DATA_SUPERS INTEGER, " +
            "$COL_INSPECTION_DATA_HONEY_FRAMES INTEGER, " +
            "$COL_INSPECTION_DATA_BROOD_FRAMES INTEGER, " +
            "$COL_INSPECTION_DATA_BROOD_ADJUSTED INTEGER, " +
            "$COL_INSPECTION_DATA_BROOD_CHANGE INTEGER, " +
            "$COL_INSPECTION_DATA_HONEY_ADJUSTED INTEGER, " +
            "$COL_INSPECTION_DATA_HONEY_CHANGE INTEGER, " +
            "$COL_INSPECTION_DATA_DRONE_FRAMES INTEGER, " +
            "$COL_INSPECTION_DATA_DRONE_ADJUSTED INTEGER, " +
            "$COL_INSPECTION_DATA_DRONE_CHANGE INTEGER, " +
            "$COL_INSPECTION_DATA_FREE_SPACE INTEGER, " +
            "$COL_INSPECTION_DATA_SUPPLEMENTAL_FEED REAL, " +
            "$COL_INSPECTION_DATA_WINTER_READY INTEGER, " +
            "$COL_INSPECTION_DATA_AGGRESSIVITY INTEGER, " +
            "$COL_INSPECTION_DATA_HONEY_HARVESTED INTEGER, " +
            "$COL_INSPECTION_DATA_ATTENTION_WORTH INTEGER, " +
            "$COL_INSPECTION_DATA_COLONY_END_STATE INTEGER, " +
            "$COL_INSPECTION_DATA_SEPARATED INTEGER, " +
            "$COL_INSPECTION_DATA_JOINED INTEGER, " +
            "FOREIGN KEY($COL_INSPECTION_ID_FK_INSPECTION_DATA) REFERENCES $TABLE_INSPECTIONS($COL_INSPECTION_ID), " +
            "FOREIGN KEY($COL_NOTE_ID_FK_INSPECTION_DATA) REFERENCES $TABLE_HIVE_NOTES($COL_HIVE_NOTE_ID), " +
            "FOREIGN KEY($COL_HIVE_ID_FK_INSPECTION_DATA) REFERENCES $TABLE_HIVES($COL_HIVE_ID))"

    internal val CREATE_TABLE_HONEY_HARVESTS = "CREATE TABLE $TABLE_HONEY_HARVESTS (" +
            "$COLUMN_HONEY_HARVESTS_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COLUMN_HIVE_ID_FK_HONEY_HARVEST INTEGER, " +
            "$COLUMN_HONEY_HARVESTS_DATE INTEGER, " +
            "$COLUMN_HONEY_HARVESTS_HONEY_FRAMES INTEGER, " +
            "FOREIGN KEY($COLUMN_HIVE_ID_FK_HONEY_HARVEST) REFERENCES $TABLE_HIVES($COL_HIVE_ID))"

    internal val CREATE_TABLE_HISTORY = "CREATE TABLE $TABLE_HISTORY (" +
            "$COL_HISTORY_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COL_HISTORY_ENTITY_ID INTEGER, " +
            "$COL_HISTORY_ENTITY_TYPE TEXT, " +
            "$COL_HISTORY_DATE INTEGER, " +
            "$COL_HISTORY_EVENT TEXT, " +
            "FOREIGN KEY($COL_HISTORY_ENTITY_ID) REFERENCES $TABLE_HIVES($COL_HIVE_ID))"

    internal val CREATE_TABLE_SUPPLEMENTED_FEED = "CREATE TABLE $TABLE_SUPPLEMENTED_FEED (" +
            "$COLUMN_SUPPLEMENTED_FEED_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COLUMN_HIVE_ID_FK_SUPPLEMENTED_FEED INTEGER, " +
            "$COLUMN_SUPPLEMENTED_FEED_DATE TEXT, " +
            "$COLUMN_SUPPLEMENTED_FEED_KILOS REAL, " +
            "FOREIGN KEY($COLUMN_HIVE_ID_FK_SUPPLEMENTED_FEED) REFERENCES $TABLE_HIVES($COL_HIVE_ID))"

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE_STATIONS)
        db?.execSQL(CREATE_TABLE_HIVES)
        db?.execSQL(CREATE_TABLE_DATA_LOGS)
        db?.execSQL(CREATE_TABLE_HIVE_NOTES)
        db?.execSQL(CREATE_TABLE_TODO)
        db?.execSQL(CREATE_TABLE_INSPECTIONS)
        db?.execSQL(CREATE_TABLE_INSPECTION_DATA)
        db?.execSQL(CREATE_TABLE_HONEY_HARVESTS)
        db?.execSQL(CREATE_TABLE_HISTORY)
        db?.execSQL(CREATE_TABLE_SUPPLEMENTED_FEED)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_STATIONS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_HIVES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_DATA_LOGS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_HIVE_NOTES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TODO")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_INSPECTIONS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_INSPECTION_DATA")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_HONEY_HARVESTS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SUPPLEMENTED_FEED")


        // Recreate tables with updated schema
        onCreate(db)
    }

    companion object {
        const val DATABASE_VERSION = 19
        const val DATABASE_NAME = "beehives.db"

        //Station
        const val TABLE_STATIONS = "tbl_stations"
        const val COL_STATION_ID = "stations_id"
        const val COL_STATION_NAME = "stations_name"
        const val COL_STATION_PLACE = "stations_place"
        const val COL_STATION_IN_USE = "stations_in_use"
        const val COL_STATION_CREATION_TIME = "station_creation_time"
        const val COL_STATION_DEATH_TIME = "station_death_time"

        //Hive
        const val TABLE_HIVES = "tbl_hives"
        const val COL_HIVE_ID = "hive_id"
        const val COL_HIVE_NAME_TAG = "hive_name_tag"
        const val COL_HIVE_QR_TAG = "hive_qr_tag"
        const val COL_STATION_ID_FK_HIVES = "station_foreign_key"
        const val COL_HIVE_FRAMES_PER_SUPER = "hive_frames_per_super"
        const val COL_HIVE_SUPERS = "hive_supers"
        const val COL_HIVE_BROOD_FRAMES = "hive_brood_frames"
        const val COL_HIVE_HONEY_FRAMES = "hive_honey_frames"
        const val COL_HIVE_DRONE_BROOD_FRAMES = "hive_drone_brood_frames"
        const val COL_HIVE_FREE_SPACE_FRAMES = "hive_free_space_frames"
        const val COL_HIVE_COLONY_ORIGIN = "hive_colony_origin"
        const val COL_HIVE_COLONY_END_STATE = "hive_colony_end_state"
        const val COL_HIVE_WINTER_READY = "hive_winter_ready"
        const val COL_HIVE_AGGRESSIVITY = "hive_aggressivity"
        const val COL_HIVE_ATTENTION_WORTH = "hive_attention_worth"
        const val COL_HIVE_CREATION_TIME = "hive_creation_time"
        const val COL_HIVE_DEATH_TIME = "hive_death_time"
        const val COL_HIVE_STATION_ORDER = "hive_station_order"

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
        const val COL_HIVE_ID_FK_TODO = "hive_foreign_key_todo"
        const val COL_TODO_TEXT = "todo_text"
        const val COL_TODO_STATE = "todo_state"
        const val COL_TODO_DATE = "todo_date"

        // Inspections
        const val TABLE_INSPECTIONS = "tbl_inspections"
        const val COL_INSPECTION_ID = "inspection_id"
        const val COL_STATION_ID_FK_INSPECTION = "station_foreign_key_inspection"
        const val COL_INSPECTION_DATE = "inspection_date"
        const val COL_INSPECTION_DATA_IDS = "inspection_data"

        // Inspection data
        const val TABLE_INSPECTION_DATA = "tbl_inspection_data"
        const val COL_INSPECTION_DATA_ID = "inspection_data_id"
        const val COL_HIVE_ID_FK_INSPECTION_DATA = "inspection_data_hive_id_fk"
        const val COL_INSPECTION_ID_FK_INSPECTION_DATA = "inspection_data_inspection_id_fk"
        const val COL_INSPECTION_DATA_FRAMES_PER_SUPER = "inspection_data_frames_per_super"
        const val COL_INSPECTION_DATA_SUPERS = "inspection_data_supers"
        const val COL_INSPECTION_DATA_BROOD_FRAMES = "inspection_data_brood_frames"
        const val COL_INSPECTION_DATA_BROOD_ADJUSTED = "inspection_data_brood_adjusted"
        const val COL_INSPECTION_DATA_BROOD_CHANGE = "inspection_data_brood_change"
        const val COL_INSPECTION_DATA_HONEY_FRAMES = "inspection_data_honey_frames"
        const val COL_INSPECTION_DATA_HONEY_ADJUSTED = "inspection_data_honey_adjusted"
        const val COL_INSPECTION_DATA_HONEY_CHANGE = "inspection_data_honey_change"
        const val COL_INSPECTION_DATA_DRONE_FRAMES = "inspection_data_drone_brood_frames"
        const val COL_INSPECTION_DATA_DRONE_ADJUSTED = "inspection_data_drone_brood_adjusted"
        const val COL_INSPECTION_DATA_DRONE_CHANGE = "inspection_data_drone_brood_change"
        const val COL_INSPECTION_DATA_FREE_SPACE = "inspection_data_free_space_frames"
        const val COL_INSPECTION_DATA_SUPPLEMENTAL_FEED = "inspection_data_supplemental_feed"
        const val COL_INSPECTION_DATA_WINTER_READY = "inspection_data_winter_ready"
        const val COL_INSPECTION_DATA_AGGRESSIVITY = "inspection_data_aggressivity"
        const val COL_INSPECTION_DATA_HONEY_HARVESTED = "inspection_data_honey_harvested"
        const val COL_INSPECTION_DATA_ATTENTION_WORTH = "inspection_data_attention_worth"
        const val COL_INSPECTION_DATA_COLONY_END_STATE = "inspection_data_colony_end_state"
        const val COL_NOTE_ID_FK_INSPECTION_DATA = "inspection_data_note_id_fk"
        const val COL_INSPECTION_DATA_JOINED = "inspection_data_joined"
        const val COL_INSPECTION_DATA_SEPARATED = "inspection_data_separated"

        // Honey harvest
        const val TABLE_HONEY_HARVESTS = "tbl_honey_harvests"
        const val COLUMN_HONEY_HARVESTS_ID = "honey_harvest_id"
        const val COLUMN_HIVE_ID_FK_HONEY_HARVEST = "honey_harvest_hiveId"
        const val COLUMN_HONEY_HARVESTS_DATE = "honey_harvest_date"
        const val COLUMN_HONEY_HARVESTS_HONEY_FRAMES = "honey_harvest_honeyFrames"


        // Supplemented feed
        const val TABLE_SUPPLEMENTED_FEED = "tbl_supplemented_feed"
        const val COLUMN_SUPPLEMENTED_FEED_ID = "supplemented_feed_id"
        const val COLUMN_HIVE_ID_FK_SUPPLEMENTED_FEED = "supplemented_feed_hiveId"
        const val COLUMN_SUPPLEMENTED_FEED_DATE = "supplemented_feed_date"
        const val COLUMN_SUPPLEMENTED_FEED_KILOS = "supplemented_feed_kilos"

        // History
        const val TABLE_HISTORY = "tbl_history"
        const val COL_HISTORY_ID = "history_id"
        const val COL_HISTORY_ENTITY_ID = "history_entity_id"
        const val COL_HISTORY_ENTITY_TYPE = "history_entity_type"
        const val COL_HISTORY_DATE = "history_date"
        const val COL_HISTORY_EVENT = "history_event"
    }
}