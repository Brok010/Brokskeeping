package com.example.brokskeeping.DbFunctionality

import android.content.ContentValues
import android.database.Cursor
import android.provider.ContactsContract.Data
import android.util.Log
import com.example.brokskeeping.DataClasses.Beehive
import com.example.brokskeeping.DataClasses.HiveNotes
import com.example.brokskeeping.DataClasses.HumTempData
import com.example.brokskeeping.DataClasses.Station
import com.example.brokskeeping.Functionality.Utils
import java.util.Date

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
        val colonyEndState = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_COLONY_END_STATE))
        val winterReady = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_WINTER_READY)) == 1
        val aggressivity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_AGGRESSIVITY))
        val attentionWorth = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_ATTENTION_WORTH))
        val creationTime = Date(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_CREATION_TIME)))
        val deathTime = Date(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_DEATH_TIME)))
        val stationOrder = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_STATION_ORDER))

        return Beehive(
            id,
            stationId,
            nameTag,
            qrTag,
            broodFrames,
            honeyFrames,
            droneBroodFrames,
            framesPerSuper,
            supers,
            freeSpaceFrames,
            colonyOrigin,
            colonyEndState,
            winterReady,
            aggressivity,
            attentionWorth,
            creationTime,
            deathTime,
            stationOrder
        )
    }

    fun forceHiveStationOrder(dbHelper: DatabaseHelper, stationId: Int, newHiveOrder: Int, hiveId: Int? = null) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // Step 1: Get total hives in the station
            val countCursor = db.rawQuery(
                """
            SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_HIVES}
            WHERE ${DatabaseHelper.COL_STATION_ID_FK_HIVES} = ?
        """.trimIndent(), arrayOf(stationId.toString())
            )
            var totalHives = 0
            if (countCursor.moveToFirst()) {
                totalHives = countCursor.getInt(0)
            }
            countCursor.close()

            val clampedOrder = newHiveOrder.coerceIn(1, totalHives)

            if (hiveId != null) {
                // Step 2: Get current order of the hive
                val currentCursor = db.rawQuery(
                    """
                SELECT ${DatabaseHelper.COL_HIVE_STATION_ORDER}
                FROM ${DatabaseHelper.TABLE_HIVES}
                WHERE ${DatabaseHelper.COL_HIVE_ID} = ?
            """.trimIndent(), arrayOf(hiveId.toString())
                )
                if (currentCursor.moveToFirst()) {
                    val currentOrder = currentCursor.getInt(0)
                    currentCursor.close()

                    if (currentOrder == clampedOrder) {
                        db.setTransactionSuccessful()
                        return  // no change needed
                    }

                    // Step 3: Temporarily move current hive to placeholder order (-1)
                    db.execSQL(
                        """
                    UPDATE ${DatabaseHelper.TABLE_HIVES}
                    SET ${DatabaseHelper.COL_HIVE_STATION_ORDER} = -1
                    WHERE ${DatabaseHelper.COL_HIVE_ID} = ?
                """.trimIndent(), arrayOf(hiveId)
                    )

                    // Step 4: Shift the other hives
                    if (currentOrder > clampedOrder) {
                        // Moving up: shift all between clampedOrder and currentOrder-1 down (+1)
                        db.execSQL(
                            """
                        UPDATE ${DatabaseHelper.TABLE_HIVES}
                        SET ${DatabaseHelper.COL_HIVE_STATION_ORDER} = ${DatabaseHelper.COL_HIVE_STATION_ORDER} + 1
                        WHERE ${DatabaseHelper.COL_STATION_ID_FK_HIVES} = ? 
                          AND ${DatabaseHelper.COL_HIVE_STATION_ORDER} >= ? 
                          AND ${DatabaseHelper.COL_HIVE_STATION_ORDER} < ?
                    """.trimIndent(), arrayOf(stationId, clampedOrder, currentOrder)
                        )
                    } else {
                        // Moving down: shift all between currentOrder+1 and clampedOrder up (-1)
                        db.execSQL(
                            """
                        UPDATE ${DatabaseHelper.TABLE_HIVES}
                        SET ${DatabaseHelper.COL_HIVE_STATION_ORDER} = ${DatabaseHelper.COL_HIVE_STATION_ORDER} - 1
                        WHERE ${DatabaseHelper.COL_STATION_ID_FK_HIVES} = ? 
                          AND ${DatabaseHelper.COL_HIVE_STATION_ORDER} > ? 
                          AND ${DatabaseHelper.COL_HIVE_STATION_ORDER} <= ?
                    """.trimIndent(), arrayOf(stationId, currentOrder, clampedOrder)
                        )
                    }

                    // Step 5: Move current hive to its new correct position
                    db.execSQL(
                        """
                    UPDATE ${DatabaseHelper.TABLE_HIVES}
                    SET ${DatabaseHelper.COL_HIVE_STATION_ORDER} = ?
                    WHERE ${DatabaseHelper.COL_HIVE_ID} = ?
                """.trimIndent(), arrayOf(clampedOrder, hiveId)
                    )
                } else {
                    currentCursor.close()
                }
            }

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e("DBHelper", "Error forcing hive station order", e)
        } finally {
            db.endTransaction()
        }
    }


    fun getAllHives(
        dbHelper: DatabaseHelper,
        stationId: Int? = null,
        dead: Int? = null,
        creationYear: Int? = null,
        creationMonth: Int? = null,
        deathYear: Int? = null,
        deathMonth: Int? = null,
        ordered: Boolean? = false
    ): Pair<List<Beehive>, Int> {
        val hives = mutableListOf<Beehive>()

        val (creationTimes, creationTimesResult) = Utils.getStartAndEndTime(creationYear, creationMonth)
        if (creationTimesResult == 0) return emptyList<Beehive>() to 0
        val (startCreationTime, endCreationTime) = creationTimes

        val (deathTimes, deathTimesResult) = Utils.getStartAndEndTime(deathYear, deathMonth)
        if (deathTimesResult == 0) return emptyList<Beehive>() to 0
        val (startDeathTime, endDeathTime) = deathTimes

        return try {
            val selectionArgs = mutableListOf<String>()
            val whereConditions = mutableListOf<String>()

            if (stationId != null && stationId != 0) {
                whereConditions.add("${DatabaseHelper.COL_STATION_ID_FK_HIVES} = ?")
                selectionArgs.add(stationId.toString())
            }

            if (dead == 1) {
                whereConditions.add("${DatabaseHelper.COL_HIVE_COLONY_END_STATE} != -1")
            } else if (dead == 0) {
                whereConditions.add("${DatabaseHelper.COL_HIVE_COLONY_END_STATE} = -1")
            }

            whereConditions.add("${DatabaseHelper.COL_HIVE_CREATION_TIME} BETWEEN ? AND ?")
            selectionArgs.add(startCreationTime.toString())
            selectionArgs.add(endCreationTime.toString())

            whereConditions.add("${DatabaseHelper.COL_HIVE_DEATH_TIME} BETWEEN ? AND ?")
            selectionArgs.add(startDeathTime.toString())
            selectionArgs.add(endDeathTime.toString())

            val whereClause = if (whereConditions.isNotEmpty()) {
                "WHERE ${whereConditions.joinToString(" AND ")}"
            } else ""

            val orderByClause = if (ordered == true) {
                "ORDER BY ${DatabaseHelper.COL_HIVE_STATION_ORDER}"
            } else ""

            val query = "SELECT * FROM ${DatabaseHelper.TABLE_HIVES} $whereClause $orderByClause"

            val cursor = dbHelper.readableDatabase.rawQuery(
                query,
                if (selectionArgs.isNotEmpty()) selectionArgs.toTypedArray() else null
            )

            cursor.use {
                while (it.moveToNext()) {
                    val hive = createHiveFromCursor(it)
                    hives.add(hive)
                }
            }

            Pair(hives, 1)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(emptyList(), 0)
        }
    }


    fun getNextAvailableHiveStationOrder(dbHelper: DatabaseHelper, stationID: Int): Pair<Int, Int> {
        val db = dbHelper.readableDatabase
        val usedOrders = mutableSetOf<Int>()

        return try {
            val cursor = db.rawQuery(
                "SELECT ${DatabaseHelper.COL_HIVE_STATION_ORDER} FROM ${DatabaseHelper.TABLE_HIVES} WHERE ${DatabaseHelper.COL_STATION_ID_FK_HIVES} = ?",
                arrayOf(stationID.toString())
            )

            cursor.use {
                while (it.moveToNext()) {
                    val order = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_STATION_ORDER))
                    if (order > 0) {
                        usedOrders.add(order)
                    }
                }
            }

            var nextOrder = 1
            while (usedOrders.contains(nextOrder)) {
                nextOrder++
            }

            Pair(nextOrder, 1)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(-1, 0) // -1 as error indicator, 0 means error occurred
        } finally {
            db.close()
        }
    }


    fun saveHive(
        dbHelper: DatabaseHelper,
        beehive: Beehive,
        fileData: String? = null,
        currentNotes: String? = null
    ): Pair<Int, Int> {
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
            put(DatabaseHelper.COL_HIVE_COLONY_END_STATE, beehive.colonyEndState)
            put(DatabaseHelper.COL_HIVE_WINTER_READY, if (beehive.winterReady) 1 else 0)
            put(DatabaseHelper.COL_HIVE_AGGRESSIVITY, beehive.aggressivity)
            put(DatabaseHelper.COL_HIVE_ATTENTION_WORTH, beehive.attentionWorth)
            put(
                DatabaseHelper.COL_HIVE_CREATION_TIME,
                if (beehive.creationTime.time != 0L) beehive.creationTime.time else System.currentTimeMillis()
            )
            put(DatabaseHelper.COL_HIVE_DEATH_TIME, beehive.deathTime.time)
            if (beehive.stationOrder < 0) {
                val (orderNum, result) = getNextAvailableHiveStationOrder(dbHelper, beehive.stationId)
                if (result == 0) {
                    Log.e("HiveFunctionality", "saveHive - couldn't retrieve stationOrder")
                }
                put(DatabaseHelper.COL_HIVE_STATION_ORDER, orderNum)
            }
        }

        val currentHiveId: Int
        val db = dbHelper.writableDatabase

        if (beehive.id > 0) {
            // Update existing hive
            val rowsUpdated = db.update(
                DatabaseHelper.TABLE_HIVES,
                values,
                "${DatabaseHelper.COL_HIVE_ID} = ?",
                arrayOf(beehive.id.toString())
            )
            currentHiveId = if (rowsUpdated > 0) beehive.id else -1
        } else {
            // Insert new hive
            currentHiveId = db.insert(DatabaseHelper.TABLE_HIVES, null, values).toInt()
        }

        db.close()

        if (currentHiveId != -1) {
            if (!fileData.isNullOrEmpty() && fileData != "None") {
                val data = HumTempData(
                    stationId = beehive.stationId,
                    hiveId = currentHiveId,
                    logText = fileData
                )
                HumTempDataFunctionality.addDataLogs(dbHelper, data)
            }
            if (!currentNotes.isNullOrEmpty() && currentNotes != "None") {
                val note = HiveNotes(
                    stationId = beehive.stationId,
                    hiveId = currentHiveId,
                    noteText = currentNotes
                )
                NotesFunctionality.addNote(dbHelper, note)
            }
            return Pair(currentHiveId, 1)
        }

        return Pair(-1, 0)
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

        //todo delete harvests and supplemented feed

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