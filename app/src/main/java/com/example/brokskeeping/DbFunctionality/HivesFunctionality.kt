package com.example.brokskeeping.DbFunctionality

import android.content.ContentValues
import android.database.Cursor
import android.provider.ContactsContract.Data
import android.util.Log
import com.example.brokskeeping.DataClasses.Beehive
import com.example.brokskeeping.DataClasses.HiveNotes
import com.example.brokskeeping.DataClasses.HumTempData
import com.example.brokskeeping.DataClasses.Station

object HivesFunctionality {

    fun getHiveIdByQRString(dbHelper: DatabaseHelper, QRString: String): Int{
        var hiveId = -1

        // Obtain a readable database from the DatabaseHelper
        val db = dbHelper.readableDatabase

        // SQL query to get the hive name by its ID
        val query = "SELECT ${DatabaseHelper.COL_HIVE_ID} FROM ${DatabaseHelper.TABLE_HIVES} WHERE ${DatabaseHelper.COL_HIVE_QR_TAG} = ?"

        // Execute the query with the provided hive ID
        val cursor = db.rawQuery(query, arrayOf(QRString))

        // Check if the cursor contains results
        cursor?.use {
            // Move to the first row in the cursor
            if (it.moveToFirst()) {
                // Get the station id using getColumnIndexOrThrow which helps with debugging
                hiveId = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_ID))
            }
        }

        // Close the database connection
        db.close()
        return hiveId
    }

    fun getStationIdByHiveId(dbHelper: DatabaseHelper, hiveId: Int): Int {
        var stationId = -1

        // Obtain a readable database from the DatabaseHelper
        val db = dbHelper.readableDatabase

        // SQL query to get the hive name by its ID
        val query = "SELECT ${DatabaseHelper.COL_STATION_ID_FK_HIVES} FROM ${DatabaseHelper.TABLE_HIVES} WHERE ${DatabaseHelper.COL_HIVE_ID} = ?"

        // Execute the query with the provided hive ID
        val cursor = db.rawQuery(query, arrayOf(hiveId.toString()))

        // Check if the cursor contains results
        cursor?.use {
            // Move to the first row in the cursor
            if (it.moveToFirst()) {
                // Get the station id using getColumnIndexOrThrow which helps with debugging
                stationId = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_STATION_ID_FK_HIVES))
            }
        }

        // Close the database connection
        db.close()
        return stationId
    }

    fun getHiveNameById(dbHelper: DatabaseHelper, hiveId: Int): String {
        var hiveName: String = ""

        // Obtain a readable database from the DatabaseHelper
        val db = dbHelper.readableDatabase

        // SQL query to get the hive name by its ID
        val query = "SELECT ${DatabaseHelper.COL_HIVE_NAME_TAG} FROM ${DatabaseHelper.TABLE_HIVES} WHERE ${DatabaseHelper.COL_HIVE_ID} = ?"

        // Execute the query with the provided hive ID
        val cursor = db.rawQuery(query, arrayOf(hiveId.toString()))

        // Check if the cursor contains results
        cursor?.use {
            // Move to the first row in the cursor
            if (it.moveToFirst()) {
                // Get the hive name using getColumnIndexOrThrow which helps with debugging
                hiveName = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_NAME_TAG))
            }
        }

        // Close the database connection
        db.close()
        return hiveName
    }

    fun getHiveAttributesById(dbHelper: DatabaseHelper, hiveId: Int): Pair<Beehive?, Int> {
        val query = "SELECT * FROM ${DatabaseHelper.TABLE_HIVES} WHERE ${DatabaseHelper.COL_HIVE_ID} = ?"
        val selectionArgs = arrayOf(hiveId.toString())
        val cursor = dbHelper.readableDatabase.rawQuery(query, selectionArgs)

        return cursor.use { cursor ->
            if (cursor.moveToFirst()) {
                val hive = createHiveFromCursor(cursor)  // Create the Beehive object
                Pair(hive, 1)  // Return the Beehive object and 1 for success
            } else {
                Pair(null, 0)  // Return null and 0 for failure
            }
        }
    }

    private fun createHiveFromCursor(cursor: Cursor): Beehive {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_ID))
        val stationId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATION_ID_FK_HIVES))
        val nameTag = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_NAME_TAG))
        val qrTag = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_QR_TAG))
        val framesPerSuper = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_FRAMES_PER_SUPER))
        val supers = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_SUPERS))
        val broodFrames = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_BROOD_FRAMES))
        val honeyFrames = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_HONEY_FRAMES))
        val droneBroodFrames = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_DRONE_BROOD_FRAMES))
        val freeSpaceFrames = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_FREE_SPACE_FRAMES))
        val colonyOrigin = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_COLONY_ORIGIN))
        val supplementedFeedCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_SUPPLEMENTED_FEED_COUNT))
        val winterReady = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_WINTER_READY)) == 1
        val aggressivity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_AGGRESSIVITY))
        val death = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_DEATH)) == 1
        val attentionWorth = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_ATTENTION_WORTH))

        return Beehive(
            id,
            stationId,
            nameTag,
            qrTag,
            broodFrames,
            honeyFrames,
            framesPerSuper,
            supers,
            droneBroodFrames,
            freeSpaceFrames,
            colonyOrigin,
            supplementedFeedCount,
            winterReady,
            aggressivity,
            death,
            attentionWorth)
    }

    fun updateHive(dbHelper: DatabaseHelper, beehive: Beehive): Int {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        var result = 0

        try {
            val values = ContentValues().apply {
                if (!beehive.nameTag.isNullOrBlank()) {
                    put(DatabaseHelper.COL_HIVE_NAME_TAG, beehive.nameTag)
                }
                if (!beehive.qrTag.isNullOrBlank()) {
                    put(DatabaseHelper.COL_HIVE_QR_TAG, beehive.qrTag)
                }
                if (beehive.stationId != -1) {
                    put(DatabaseHelper.COL_STATION_ID_FK_HIVES, beehive.stationId)
                }
                if (beehive.broodFrames != -1) {
                    put(DatabaseHelper.COL_HIVE_BROOD_FRAMES, beehive.broodFrames)
                }
                if (beehive.honeyFrames != -1) {
                    put(DatabaseHelper.COL_HIVE_HONEY_FRAMES, beehive.honeyFrames)
                }
                if (beehive.framesPerSuper != -1) {
                    put(DatabaseHelper.COL_HIVE_FRAMES_PER_SUPER, beehive.framesPerSuper)
                }
                if (beehive.supers != -1) {
                    put(DatabaseHelper.COL_HIVE_SUPERS, beehive.supers)
                }
                if (beehive.droneBroodFrames != -1) {
                    put(DatabaseHelper.COL_HIVE_DRONE_BROOD_FRAMES, beehive.droneBroodFrames)
                }
                if (beehive.freeSpaceFrames != -1) {
                    put(DatabaseHelper.COL_HIVE_FREE_SPACE_FRAMES, beehive.freeSpaceFrames)
                }
                if (!beehive.colonyOrigin.isNullOrBlank()) {
                    put(DatabaseHelper.COL_HIVE_COLONY_ORIGIN, beehive.colonyOrigin)
                }
                if (beehive.supplementedFeedCount != -1) {
                    put(DatabaseHelper.COL_HIVE_SUPPLEMENTED_FEED_COUNT, beehive.supplementedFeedCount)
                }
                put(DatabaseHelper.COL_HIVE_WINTER_READY, if (beehive.winterReady) 1 else 0)
                if (beehive.aggressivity != -1) {
                    put(DatabaseHelper.COL_HIVE_AGGRESSIVITY, beehive.aggressivity)
                }
                put(DatabaseHelper.COL_HIVE_DEATH, if (beehive.death) 1 else 0)
                if (beehive.attentionWorth != -1) {
                    put(DatabaseHelper.COL_HIVE_ATTENTION_WORTH, beehive.attentionWorth)
                }
            }

            if (values.size() > 0) {
                val rowsUpdated = db.update(
                    DatabaseHelper.TABLE_HIVES,
                    values,
                    "${DatabaseHelper.COL_HIVE_ID} = ?",
                    arrayOf(beehive.id.toString())
                )

                if (rowsUpdated > 0) {
                    db.setTransactionSuccessful()
                    result = 1
                } else {
                    Log.e("DatabaseHelper", "No rows updated for hive ID ${beehive.id}")
                }
            } else {
                Log.w("DatabaseHelper", "No valid fields to update for hive ID ${beehive.id}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }

        return result
    }



    fun getAllHives(dbHelper: DatabaseHelper, stationId: Int? = null, dead: Int? = null): Pair<List<Beehive>, Int> {
        val hives = mutableListOf<Beehive>()

        return try {
            val selectionArgs = mutableListOf<String>()
            val whereConditions = mutableListOf<String>()

            if (stationId != null && stationId != 0) {
                whereConditions.add("${DatabaseHelper.COL_STATION_ID_FK_HIVES} = ?")
                selectionArgs.add(stationId.toString())
            }

            if (dead == 1) {
                whereConditions.add("${DatabaseHelper.COL_HIVE_DEATH} = 1")
            } else if (dead == 0) {
                whereConditions.add("${DatabaseHelper.COL_HIVE_DEATH} = 0")
            }

            val whereClause = if (whereConditions.isNotEmpty()) {
                "WHERE ${whereConditions.joinToString(" AND ")}"
            } else {
                ""
            }

            val query = "SELECT * FROM ${DatabaseHelper.TABLE_HIVES} $whereClause"

            val cursor = dbHelper.readableDatabase.rawQuery(query, if (selectionArgs.isNotEmpty()) selectionArgs.toTypedArray() else null)

            cursor.use {
                while (it.moveToNext()) {
                    val hive = createHiveFromCursor(it)
                    hives.add(hive)
                }
            }

            Pair(hives, 1) // Success
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(emptyList(), 0) // Failure
        }
    }




    //TODO: get all notes and datalogs from the database and pass them along?

    fun saveHive(
        dbHelper: DatabaseHelper,
        beehive: Beehive,
        fileData: String,
        currentNotes: String
    ): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_STATION_ID_FK_HIVES, beehive.stationId)
            put(DatabaseHelper.COL_HIVE_NAME_TAG, beehive.nameTag)
            put(DatabaseHelper.COL_HIVE_QR_TAG, beehive.qrTag)
            put(DatabaseHelper.COL_HIVE_FRAMES_PER_SUPER, beehive.framesPerSuper)
            put(DatabaseHelper.COL_HIVE_SUPERS, beehive.supers)
            put(DatabaseHelper.COL_HIVE_BROOD_FRAMES, beehive.broodFrames)
            put(DatabaseHelper.COL_HIVE_HONEY_FRAMES, beehive.honeyFrames)
            put(DatabaseHelper.COL_HIVE_DRONE_BROOD_FRAMES, beehive.droneBroodFrames)
            put(DatabaseHelper.COL_HIVE_FREE_SPACE_FRAMES, beehive.freeSpaceFrames)
            put(DatabaseHelper.COL_HIVE_COLONY_ORIGIN, beehive.colonyOrigin)
            put(DatabaseHelper.COL_HIVE_WINTER_READY, if (beehive.winterReady) 1 else 0)
            put(DatabaseHelper.COL_HIVE_SUPPLEMENTED_FEED_COUNT, beehive.supplementedFeedCount)
            put(DatabaseHelper.COL_HIVE_AGGRESSIVITY, beehive.aggressivity)
            put(DatabaseHelper.COL_HIVE_DEATH, if (beehive.death) 1 else 0)
            put(DatabaseHelper.COL_HIVE_ATTENTION_WORTH, beehive.attentionWorth)
        }

        val currentHiveId = db.insert(DatabaseHelper.TABLE_HIVES, null, values).toInt()
        db.close()

        if (currentHiveId != -1) {
            if (fileData != "None") {
                val data = HumTempData(
                    stationId = beehive.stationId,
                    hiveId = currentHiveId,
                    logText = fileData
                )
                HumTempDataFunctionality.addDataLogs(dbHelper, data)
            }
            if (currentNotes != "None") {
                val note = HiveNotes(
                    stationId = beehive.stationId,
                    hiveId = currentHiveId,
                    noteText = currentNotes
                )
                NotesFunctionality.addNote(dbHelper, note)
            }
            return 1
        }

        return 0
    }

    fun deleteHive(dbHelper: DatabaseHelper, stationId: Int, hiveId: Int) {
        if (NotesFunctionality.deleteNotes(dbHelper, hiveId) == 0) {
            Log.e("HiveFunctionality", "deleteHive - deleteNotes - end with 0")
        }
        if (HumTempDataFunctionality.deleteHivesHumTempData(dbHelper, hiveId) == 0) {
            Log.e("HiveFunctionality", "deleteHive - deleteHivesHumTempData - end with 0")
        }
        if (ToDoFunctionality.deleteToDos(dbHelper, hiveId) == 0) {
            Log.e("HiveFunctionality", "deleteHive - deleteToDos - end with 0")
        }
        val (inspectionData, result) = InspectionsFunctionality.getAllInspectionDataForHiveId(dbHelper, hiveId)
        if (result == 1) {
            // Use map to delete each inspection data by id
            inspectionData.map { insData ->
                InspectionsFunctionality.deleteInspectionData(dbHelper, insData.id)
            }
        }

        val db = dbHelper.writableDatabase
        db.beginTransaction()

        try {
            // Delete the hive
            val rowsDeleted = db.delete(
                DatabaseHelper.TABLE_HIVES, "${DatabaseHelper.COL_HIVE_ID} = ? AND ${DatabaseHelper.COL_STATION_ID_FK_HIVES} = ?",
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
    }
}