package com.example.brokskeeping.DbFunctionality

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.example.brokskeeping.DataClasses.HiveNotes
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
    fun addNote(dbHelper: DatabaseHelper, note: HiveNotes): Pair<Int, Int> {
        val db = dbHelper.writableDatabase

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
        val insertedId = db.insert(DatabaseHelper.TABLE_HIVE_NOTES, null, values)

        db.close()

        return if (insertedId != -1L) {
            Pair(insertedId.toInt(), 1)  // success
        } else {
            Pair(-1, 0)  // failure
        }
    }




    // Function to delete notes associated with a specific hive
    fun deleteNotes(dbHelper: DatabaseHelper, hiveId: Int): Int {
        val db = dbHelper.writableDatabase
        val rowsAffected = db.delete(DatabaseHelper.TABLE_HIVE_NOTES, "${DatabaseHelper.COL_HIVE_ID_FK_HIVE_NOTES} = ?", arrayOf(hiveId.toString()))
        db.close()

        return if (rowsAffected > 0) 1 else 0
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