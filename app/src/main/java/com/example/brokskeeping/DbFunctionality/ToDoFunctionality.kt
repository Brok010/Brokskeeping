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

    fun addToDo(dbHelper: DatabaseHelper, toDo: ToDo) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.COL_HIVE_ID_FK_TODO, toDo.hiveId) // Set the hiveId
            put(DatabaseHelper.COL_TODO_TEXT, toDo.toDoText)
            put(DatabaseHelper.COL_TODO_STATE, if (toDo.toDoState) 1 else 0) // Convert boolean to integer
            put(DatabaseHelper.COL_TODO_DATE, toDo.date.time) // Convert date to milliseconds
        }

        db.insert(DatabaseHelper.TABLE_TODO, null, values)
        db.close()
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

    fun setToDone(dbHelper: DatabaseHelper, toDoId: Int) {
        val db = dbHelper.writableDatabase
        val contentValues = ContentValues()

        // Set the state to 1 (done)
        contentValues.put(DatabaseHelper.COL_TODO_STATE, 1)

        // Perform the update operation
        db.update(
            DatabaseHelper.TABLE_TODO,
            contentValues,
            "${DatabaseHelper.COL_TODO_ID} = ?",
            arrayOf(toDoId.toString())
        )
        db.close()
    }

    fun getAllPendingToDos(dbHelper: DatabaseHelper, hiveId: Int): List<ToDo> {
        val todos = mutableListOf<ToDo>()
        val db = dbHelper.readableDatabase
        val currentTimeMillis = System.currentTimeMillis()

        val selection = "${DatabaseHelper.COL_HIVE_ID_FK_TODO} = ? AND " +
                        "${DatabaseHelper.COL_TODO_STATE} = ? AND " +
                        "${DatabaseHelper.COL_TODO_DATE} < ?"

        val selectionArgs = arrayOf(hiveId.toString(), "0", currentTimeMillis.toString())

        val cursor = db.query(DatabaseHelper.TABLE_TODO, null, selection, selectionArgs, null, null, null)

        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                val todo = createToDoFromCursor(cursor)
                // Filter out ToDos where the date has not yet passed
                if (todo.date.time < currentTimeMillis) {
                    todos.add(todo)
                }
            }
        }

        cursor.close()
        db.close()

        return todos
    }
    fun deleteToDos(dbHelper: DatabaseHelper, hiveId: Int) {
        val db = dbHelper.writableDatabase
        db.delete(DatabaseHelper.TABLE_TODO, "${DatabaseHelper.COL_HIVE_ID_FK_TODO} = ?", arrayOf(hiveId.toString()))
        db.close()
    }

    fun deleteToDo(dbHelper: DatabaseHelper, toDoId: Int) {
        val db = dbHelper.writableDatabase
        val selection = "${DatabaseHelper.COL_TODO_ID} = ?"
        val selectionArgs = arrayOf(toDoId.toString())
        db.delete(DatabaseHelper.TABLE_TODO, selection, selectionArgs)
        db.close()
    }

    fun getAllToDos(dbHelper: DatabaseHelper, hiveId: Int): List<ToDo> {
        val todos = mutableListOf<ToDo>()
        val db = dbHelper.readableDatabase
        val selection = "${DatabaseHelper.COL_HIVE_ID_FK_TODO} = ?"
        val selectionArgs = arrayOf(hiveId.toString())

        val cursor =
            db.query(DatabaseHelper.TABLE_TODO, null, selection, selectionArgs, null, null, null)

        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                todos.add(createToDoFromCursor(cursor))
            }
        }

        cursor.close()
        db.close()

        return todos
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