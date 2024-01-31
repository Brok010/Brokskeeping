package com.example.brokskeeping

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import android.widget.Toast
import com.example.brokskeeping.Classes.HiveNotes
import com.example.brokskeeping.Classes.HumTempData
import com.example.brokskeeping.Classes.Station
import java.util.Date

object NotesFunctionality {
    fun getNote(dbHelper: DatabaseHelper, noteId: Int): HiveNotes {
        val db = dbHelper.readableDatabase

        val query = "SELECT * FROM ${DatabaseHelper.TABLE_HIVE_NOTES} WHERE " +
                "${DatabaseHelper.COL_HIVE_NOTE_ID} = ?"
        val selectionArgs = arrayOf(noteId.toString())
        val cursor = db.rawQuery(query, selectionArgs)

        return if (cursor.moveToFirst()) {
            getNoteFromCursor(cursor)
        } else {
            Log.e("DatabaseHelper", "original note not found")
            return HiveNotes()
        }
    }

    private fun getNoteFromCursor(cursor: Cursor): HiveNotes {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_NOTE_ID))
        val hiveId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_ID_FK_HIVE_NOTES))
        val stationId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATION_ID_FK_HIVE_NOTES))
        val noteText = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_NOTE_TEXT))
        val noteDate = Date(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_NOTE_DATE)))

        return HiveNotes(id, hiveId, stationId, noteText, noteDate)
    }

    fun adjustNote(dbHelper: DatabaseHelper, updatedNote: HiveNotes) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()

        try {
            // Convert the date to a Unix timestamp
            val unixTimestamp = updatedNote.noteDate.time

            val values = ContentValues().apply {
                put(DatabaseHelper.COL_HIVE_NOTE_TEXT, updatedNote.noteText)
                put(DatabaseHelper.COL_HIVE_NOTE_DATE, unixTimestamp)
            }

            val rowsUpdated = db.update(
                DatabaseHelper.TABLE_HIVE_NOTES, values, "${DatabaseHelper.COL_HIVE_NOTE_ID} = ?",
                arrayOf(updatedNote.id.toString()))

            if (rowsUpdated > 0) {
                db.setTransactionSuccessful()
            } else {
                Log.e("DatabaseHelper", "Failed to update note with ID ${updatedNote.id}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }
    fun addFirstHiveNote(dbHelper: DatabaseHelper, data: HiveNotes) {
        val db = dbHelper.writableDatabase

        // Prepare the data to be inserted
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_STATION_ID_FK_HIVE_NOTES, data.stationId)
            put(DatabaseHelper.COL_HIVE_ID_FK_HIVE_NOTES, data.hiveId)
            put(DatabaseHelper.COL_HIVE_NOTE_TEXT, data.noteText)

            // Check if the date was set to the default value
            val dateToInsert = if (data.noteDate == Date(0)) {
                Date(System.currentTimeMillis())
            } else {
                data.noteDate
            }

            // Convert Date to Unix timestamp (milliseconds since the epoch)
            put(DatabaseHelper.COL_HIVE_NOTE_DATE, dateToInsert.time)
        }

        // Insert the data into the HIVE_NOTES table
        db.insert(DatabaseHelper.TABLE_HIVE_NOTES, null, values)

        db.close()
    }
    fun addNote(dbHelper: DatabaseHelper, note: HiveNotes) {
        // Get a writable database instance
        val db = dbHelper.writableDatabase

        // Create a ContentValues object to hold the values to be inserted
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_STATION_ID_FK_HIVE_NOTES, note.stationId)
            put(DatabaseHelper.COL_HIVE_ID_FK_HIVE_NOTES, note.hiveId)
            put(DatabaseHelper.COL_HIVE_NOTE_TEXT, note.noteText)
            if (note.noteDate == Date(0)) {
                put(DatabaseHelper.COL_HIVE_NOTE_DATE, System.currentTimeMillis())
            } else {
                put(DatabaseHelper.COL_HIVE_NOTE_DATE, note.noteDate.time)
            }
        }

        // Insert the new row into the table
        db.insert(DatabaseHelper.TABLE_HIVE_NOTES, null, values)

        // Close the database connection
        db.close()
    }



    // Function to delete notes associated with a specific hive
    //TODO: test if it works
    fun deleteNotes(dbHelper: DatabaseHelper, hiveId: Int) {
        val db = dbHelper.writableDatabase
        db.delete(DatabaseHelper.TABLE_HIVE_NOTES, "${DatabaseHelper.COL_HIVE_ID_FK_HIVE_NOTES} = ?", arrayOf(hiveId.toString()))
        db.close()
    }

    fun deleteNote(dbHelper: DatabaseHelper, noteId: Int, hiveId: Int) {
        // Get a writable database instance
        val db = dbHelper.writableDatabase

        // Define the WHERE clause
        val selection = "${DatabaseHelper.COL_HIVE_NOTE_ID} = ? AND ${DatabaseHelper.COL_HIVE_ID_FK_HIVE_NOTES} = ?"

        // Define the values for the WHERE clause
        val selectionArgs = arrayOf(noteId.toString(), hiveId.toString())

        // Delete the row(s) from the table
        db.delete(DatabaseHelper.TABLE_HIVE_NOTES, selection, selectionArgs)

        // Close the database connection
        db.close()
    }
    fun getAllNotes(dbHelper: DatabaseHelper, hiveId: Int): List<HiveNotes> {
        val notesList = mutableListOf<HiveNotes>()
        val db = dbHelper.readableDatabase
        val selectQuery = "SELECT * FROM ${DatabaseHelper.TABLE_HIVE_NOTES} WHERE ${DatabaseHelper.COL_HIVE_ID_FK_HIVE_NOTES} = ?"
        val selectionArgs = arrayOf(hiveId.toString())

        val cursor = db.rawQuery(selectQuery, selectionArgs)
        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                val hiveNote = getNoteFromCursor(cursor)
                notesList.add(hiveNote)
            }
        }
        return notesList
    }
}