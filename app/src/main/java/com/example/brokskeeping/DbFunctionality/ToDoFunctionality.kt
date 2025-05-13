package com.example.brokskeeping.DbFunctionality

import android.content.ContentValues
import android.database.Cursor
import com.example.brokskeeping.DataClasses.ToDo
import java.util.Calendar
import java.util.Date

object ToDoFunctionality {

    fun adjustToDo(dbHelper: DatabaseHelper, newToDo: ToDo) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.COL_TODO_TEXT, newToDo.toDoText)
            put(DatabaseHelper.COL_TODO_STATE, if (newToDo.toDoState) 1 else 0)
            put(DatabaseHelper.COL_TODO_DATE, newToDo.date.time)
        }

        val selection = "${DatabaseHelper.COL_TODO_ID} = ? AND ${DatabaseHelper.COL_HIVE_ID_FK_TODO} = ?"
        val selectionArgs = arrayOf(newToDo.id.toString(), newToDo.hiveId.toString())

        db.update(DatabaseHelper.TABLE_TODO, values, selection, selectionArgs)
        db.close()
    }

    fun getToDo(dbHelper: DatabaseHelper, toDoId: Int): ToDo {
        val db = dbHelper.readableDatabase
        val selection = "${DatabaseHelper.COL_TODO_ID} = ?"
        val selectionArgs = arrayOf(toDoId.toString())

        val cursor = db.query(
            DatabaseHelper.TABLE_TODO,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        var toDo = ToDo()
        cursor.use { cursor ->
            if (cursor.moveToFirst()) {
                toDo = createToDoFromCursor(cursor)
            }
        }
        cursor.close()
        db.close()

        return toDo
    }

    fun addToDo(dbHelper: DatabaseHelper, toDo: ToDo): Int {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.COL_HIVE_ID_FK_TODO, toDo.hiveId)
            put(DatabaseHelper.COL_TODO_TEXT, toDo.toDoText)
            put(DatabaseHelper.COL_TODO_STATE, if (toDo.toDoState) 1 else 0)
            put(DatabaseHelper.COL_TODO_DATE, toDo.date.time)
        }

        val result = db.insert(DatabaseHelper.TABLE_TODO, null, values)
        db.close()

        return if (result != -1L) 1 else 0
    }

    fun postpone(dbHelper: DatabaseHelper, toDoId: Int) {
        val db = dbHelper.writableDatabase
        val contentValues = ContentValues()

        // Calculate the date 6 days from now
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 6)
        val newDateMillis = calendar.timeInMillis

        // Prepare the values to update
        contentValues.put(DatabaseHelper.COL_TODO_DATE, newDateMillis)

        // Perform the update operation
        db.update(
            DatabaseHelper.TABLE_TODO,
            contentValues,
            "${DatabaseHelper.COL_TODO_ID} = ?",
            arrayOf(toDoId.toString())
        )
        db.close()
    }

    fun toggleToDoState(dbHelper: DatabaseHelper, toDoId: Int) {
        val db = dbHelper.writableDatabase

        val cursor = db.query(
            DatabaseHelper.TABLE_TODO,
            arrayOf(DatabaseHelper.COL_TODO_STATE),
            "${DatabaseHelper.COL_TODO_ID} = ?",
            arrayOf(toDoId.toString()),
            null,
            null,
            null
        )

        var currentState = -1
        if (cursor.moveToFirst()) {
            currentState = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TODO_STATE))
        }
        cursor.close()

        val newState = if (currentState == 0) 1 else 0
        val contentValues = ContentValues().apply {
            put(DatabaseHelper.COL_TODO_STATE, newState)
        }

        db.update(
            DatabaseHelper.TABLE_TODO,
            contentValues,
            "${DatabaseHelper.COL_TODO_ID} = ?",
            arrayOf(toDoId.toString())
        )

        db.close()
    }

    fun deleteToDos(dbHelper: DatabaseHelper, hiveId: Int): Int {
        val db = dbHelper.writableDatabase
        val deletedRows = db.delete(
            DatabaseHelper.TABLE_TODO,
            "${DatabaseHelper.COL_HIVE_ID_FK_TODO} = ?",
            arrayOf(hiveId.toString())
        )
        db.close()
        return if (deletedRows > 0) 1 else 0
    }


    fun deleteToDo(dbHelper: DatabaseHelper, toDoId: Int) {
        val db = dbHelper.writableDatabase
        val selection = "${DatabaseHelper.COL_TODO_ID} = ?"
        val selectionArgs = arrayOf(toDoId.toString())
        db.delete(DatabaseHelper.TABLE_TODO, selection, selectionArgs)
        db.close()
    }

    fun getAllToDos(dbHelper: DatabaseHelper, hiveId: Int, stationId: Int, state: Int, pending: Int): Pair<List<ToDo>, Int> {
        // hiveID is enough, else stationId for all station's t0d0s, state if i want only finished ones or not
        // pending if i want the one that are not finished and pending
        val todos = mutableListOf<ToDo>()

        return try {
            val hiveIds: List<Int> = when {
                hiveId > 0 -> listOf(hiveId)    // if hive id > 0 process that one
                hiveId < 1 && stationId > 0 -> {   // if station id > 0 and hive = 0 process all for that station
                    val ids = StationsFunctionality.GetHiveIdsOfStation(dbHelper, stationId)
                    ids
                }

                else -> {   // else get all hives
                    val (hives, result) = HivesFunctionality.getAllHives(dbHelper, dead = 0)
                    if (result == 1) {
                        hives.map { it.id }
                    } else {
                        emptyList()
                    }
                }
            }

            for (id in hiveIds) {
                val db = dbHelper.readableDatabase

                val selectionBuilder = StringBuilder("${DatabaseHelper.COL_HIVE_ID_FK_TODO} = ?")
                val selectionArgs = mutableListOf(id.toString())

                if (pending == 1) {
                    val currentTime = System.currentTimeMillis()
                    selectionBuilder.append(" AND ${DatabaseHelper.COL_TODO_DATE} < ?")
                    selectionArgs.add(currentTime.toString())

                    // Force state = 0 for pending
                    selectionBuilder.append(" AND ${DatabaseHelper.COL_TODO_STATE} = ?")
                    selectionArgs.add("0")

                } else if (state == 0 || state == 1) {
                    selectionBuilder.append(" AND ${DatabaseHelper.COL_TODO_STATE} = ?")
                    selectionArgs.add(state.toString())
                }

                val cursor = db.query(
                    DatabaseHelper.TABLE_TODO,
                    null,
                    selectionBuilder.toString(),
                    selectionArgs.toTypedArray(),
                    null,
                    null,
                    null
                )

                cursor.use {
                    while (it.moveToNext()) {
                        todos.add(createToDoFromCursor(it))
                    }
                }

                db.close()
            }

            Pair(todos, 1) // success

        } catch (e: Exception) {
            e.printStackTrace()
            Pair(emptyList(), 0) // failure
        }
    }


    private fun createToDoFromCursor(cursor: Cursor): ToDo {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TODO_ID))
        val hiveId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_ID_FK_TODO))
        val toDoText = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TODO_TEXT))
        val toDoStateInt = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TODO_STATE))
        val toDoState = toDoStateInt != 0
        val dateInMillis = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TODO_DATE))
        val date = Date(dateInMillis)

        return ToDo(id, hiveId, toDoText, toDoState, date)
    }
}