package com.example.brokskeeping.DbFunctionality

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.example.brokskeeping.DataClasses.Inspection
import com.example.brokskeeping.DataClasses.InspectionData
import com.example.brokskeeping.Functionality.Utils
import java.sql.Date

object InspectionsFunctionality {

    fun getAllInspectionDataById(dbHelper: DatabaseHelper, inspectionDataId: Int): Pair<InspectionData?, Int> {
        val query =
            "SELECT * FROM ${DatabaseHelper.TABLE_INSPECTION_DATA} WHERE ${DatabaseHelper.COL_INSPECTION_DATA_ID} = ?"
        val selectionArgs = arrayOf(inspectionDataId.toString())
        val cursor = dbHelper.readableDatabase.rawQuery(query, selectionArgs)
        var returnCode = 0;
        var inspectionData: InspectionData? = null

        cursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                inspectionData = createInspectionDataFromCursor(cursor)
                returnCode = 1 // OK - Data found
            } else {
                returnCode = 0 // No data found
            }
        } ?: run {
            returnCode = 0 // Cursor is null (treated as no data in this version)
        }

        return Pair(inspectionData, returnCode)
    }

    fun deleteInspectionData(dbHelper: DatabaseHelper, inspectionDataId: Int) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()

        try {
            // Step 1: Delete from InspectionData table
            db.delete(
                DatabaseHelper.TABLE_INSPECTION_DATA,
                "${DatabaseHelper.COL_INSPECTION_DATA_ID} = ?",
                arrayOf(inspectionDataId.toString())
            )

            // Step 2: Search inspections and update their CSVs
            val cursor = db.rawQuery(
                "SELECT ${DatabaseHelper.COL_INSPECTION_ID}, ${DatabaseHelper.COL_INSPECTION_DATA_IDS} FROM ${DatabaseHelper.TABLE_INSPECTIONS}",
                null
            )

            while (cursor.moveToNext()) {
                val inspectionId = cursor.getInt(0)
                val dataIdsCsv = cursor.getString(1) ?: ""

                val updatedIds = dataIdsCsv
                    .split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it != inspectionDataId }

                val newCsv = updatedIds.joinToString(",")

                // Step 3: Update the inspection if the list changed
                if (newCsv != dataIdsCsv) {
                    val contentValues = ContentValues().apply {
                        put(DatabaseHelper.COL_INSPECTION_DATA_IDS, newCsv)
                    }
                    db.update(
                        DatabaseHelper.TABLE_INSPECTIONS,
                        contentValues,
                        "${DatabaseHelper.COL_INSPECTION_ID} = ?",
                        arrayOf(inspectionId.toString())
                    )
                }
            }

            cursor.close()
            db.setTransactionSuccessful()

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("DatabaseHelper", "Error deleting inspection data: ${e.message}")
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun getAllInspectionDataForHiveId(dbHelper: DatabaseHelper, hiveId: Int): Pair<List<InspectionData>, Int> {
        val inspectionDataList = mutableListOf<InspectionData>()
        val query = "SELECT * FROM ${DatabaseHelper.TABLE_INSPECTION_DATA} WHERE ${DatabaseHelper.COL_HIVE_ID_FK_INSPECTION_DATA} = ?"
        val selectionArgs = arrayOf(hiveId.toString())

        // Perform the query and retrieve the results
        val cursor = dbHelper.readableDatabase.rawQuery(query, selectionArgs)

        cursor.use {
            if (cursor.moveToFirst()) {
                // Iterate through the cursor and map to InspectionData objects using the provided function
                do {
                    val inspectionData = createInspectionDataFromCursor(cursor)
                    inspectionDataList.add(inspectionData)
                } while (cursor.moveToNext())

                // Return the list and success code (1)
                return Pair(inspectionDataList, 1)
            } else {
                // No data found, return an empty list and failure code (0)
                return Pair(inspectionDataList, 0)
            }
        }
    }

    fun getAllInspectionDataForInspectionId(dbHelper: DatabaseHelper, inspectionId: Int): Pair<List<InspectionData>, Int> {
        val (inspectionDataIds, idStatus) = getInspectionDataIdsByInspectionId(dbHelper, inspectionId)
        if (idStatus == 0) return Pair(emptyList(), 0)

        val inspectionDataList = mutableListOf<InspectionData>()
        val dbReadable = dbHelper.readableDatabase

        try {
            for (id in inspectionDataIds) {
                val cursor = dbReadable.rawQuery(
                    "SELECT * FROM ${DatabaseHelper.TABLE_INSPECTION_DATA} WHERE ${DatabaseHelper.COL_INSPECTION_DATA_ID} = ?",
                    arrayOf(id.toString())
                )

                if (cursor.moveToFirst()) {
                    val inspectionData = createInspectionDataFromCursor(cursor)
                    inspectionDataList.add(inspectionData)
                }
                cursor.close()
            }
            return Pair(inspectionDataList, 1)
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair(emptyList(), 0)
        } finally {
            dbReadable.close()
        }
    }


    fun getInspectionDataIdsByInspectionId(db: DatabaseHelper, inspectionId: Int): Pair<List<Int>, Int> {
        val dbReadable = db.readableDatabase

        val cursor = dbReadable.rawQuery(
            "SELECT ${DatabaseHelper.COL_INSPECTION_DATA_IDS} FROM ${DatabaseHelper.TABLE_INSPECTIONS} WHERE ${DatabaseHelper.COL_INSPECTION_ID} = ?",
            arrayOf(inspectionId.toString())
        )

        return try {
            if (cursor.moveToFirst()) {
                val dataIdsString = cursor.getString(0)
                val idList = dataIdsString.split(",").mapNotNull { it.trim().toIntOrNull() }
                Pair(idList, 1)
            } else {
                Pair(emptyList(), 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(emptyList(), 0)
        } finally {
            cursor.close()
            dbReadable.close()
        }
    }


    fun saveInspection(dbHelper: DatabaseHelper, inspection: Inspection): Int {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.COL_STATION_ID_FK_INSPECTION, inspection.stationId)
            put(DatabaseHelper.COL_INSPECTION_FINISHED, if (inspection.finished) 1 else 0)
            put(DatabaseHelper.COL_INSPECTION_DATE, inspection.date.time) // Date to timestamp
            put(DatabaseHelper.COL_INSPECTION_DATA_IDS, inspection.inspectionDataIds.joinToString(",")) // List<Int> to CSV
        }

        val result = db.insert(DatabaseHelper.TABLE_INSPECTIONS, null, values)
        return if (result == -1L) 0 else 1
    }


    fun saveInspectionData(dbHelper: DatabaseHelper, inspection: InspectionData): Pair<Int, Int> {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.COL_HIVE_ID_FK_INSPECTION_DATA, inspection.hiveId)
            put(DatabaseHelper.COL_NOTE_ID_FK_INSPECTION_DATA, inspection.noteId)
            put(DatabaseHelper.COL_INSPECTION_DATA_FRAMES_PER_SUPER, inspection.framesPerSuper)
            put(DatabaseHelper.COL_INSPECTION_DATA_SUPERS, inspection.supers)
            put(DatabaseHelper.COL_INSPECTION_DATA_HONEY_FRAMES, inspection.honeyFrames)
            put(DatabaseHelper.COL_INSPECTION_DATA_BROOD_FRAMES, inspection.broodFrames)
            put(DatabaseHelper.COL_INSPECTION_DATA_BROOD_ADJUSTED, if (inspection.broodFramesAdjusted) 1 else 0)
            put(DatabaseHelper.COL_INSPECTION_DATA_BROOD_CHANGE, inspection.broodFramesChange)
            put(DatabaseHelper.COL_INSPECTION_DATA_HONEY_ADJUSTED, if (inspection.honeyFramesAdjusted) 1 else 0)
            put(DatabaseHelper.COL_INSPECTION_DATA_HONEY_CHANGE, inspection.honeyFramesChange)
            put(DatabaseHelper.COL_INSPECTION_DATA_DRONE_FRAMES, inspection.droneBroodFrames)
            put(DatabaseHelper.COL_INSPECTION_DATA_DRONE_ADJUSTED, if (inspection.droneBroodFramesAdjusted) 1 else 0)
            put(DatabaseHelper.COL_INSPECTION_DATA_DRONE_CHANGE, inspection.droneBroodFramesChange)
            put(DatabaseHelper.COL_INSPECTION_DATA_FREE_SPACE, inspection.freeSpaceFrames)
            put(DatabaseHelper.COL_INSPECTION_DATA_SUPPLEMENTAL_FEED, if (inspection.supplementedFeed) 1 else 0)
            put(DatabaseHelper.COL_INSPECTION_DATA_WINTER_READY, if (inspection.winterReady) 1 else 0)
            put(DatabaseHelper.COL_INSPECTION_DATA_AGGRESSIVITY, inspection.aggressivity)
            put(DatabaseHelper.COL_INSPECTION_DATA_HONEY_HARVESTED, inspection.honeyHarvested)
        }

        val insertedId = db.insert(DatabaseHelper.TABLE_INSPECTION_DATA, null, values)
        val resultCode = if (insertedId == -1L) 0 else 1

        return Pair(insertedId.toInt(), resultCode)
    }



    fun getAllInspections(dbHelper: DatabaseHelper): List<Inspection> {
        val inspections = mutableListOf<Inspection>()

        val query = "SELECT * FROM ${DatabaseHelper.TABLE_INSPECTIONS}"

        val cursor = dbHelper.readableDatabase.rawQuery(query, null)

        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                val inspection = createInspectionFromCursor(cursor)
                inspections.add(inspection)
            }
        }
        return inspections
    }

    private fun createInspectionFromCursor(cursor: Cursor): Inspection {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSPECTION_ID))
        val stationId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATION_ID_FK_INSPECTION))
        val finished = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSPECTION_FINISHED)) == 1 //into bool conversion
        val dateInMillis = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSPECTION_DATE))
        val date = Date(dateInMillis)
        val inspectionDataIdsText = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSPECTION_DATA_IDS))
        val inspectionDataIds = Utils.parseStringToListOfIntegers(inspectionDataIdsText)

        return Inspection(id, stationId, finished, date, inspectionDataIds)
    }

    private fun createInspectionDataFromCursor(cursor: Cursor): InspectionData {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSPECTION_DATA_ID))
        val hiveId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIVE_ID_FK_INSPECTION_DATA))
        val noteId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTE_ID_FK_INSPECTION_DATA))
        val broodFrames = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSPECTION_DATA_BROOD_FRAMES))
        val broodFramesAdjusted = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSPECTION_DATA_BROOD_ADJUSTED)) == 1
        val broodFramesChange = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSPECTION_DATA_BROOD_CHANGE))
        val honeyFrames = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSPECTION_DATA_HONEY_FRAMES))
        val honeyFramesAdjusted = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSPECTION_DATA_HONEY_ADJUSTED)) == 1
        val honeyFramesChange = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSPECTION_DATA_HONEY_CHANGE))
        val freeSpaceFrames = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSPECTION_DATA_FREE_SPACE))
        val droneBroodFrames = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSPECTION_DATA_DRONE_FRAMES))
        val droneBroodFramesAdjusted = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSPECTION_DATA_DRONE_ADJUSTED)) == 1
        val droneBroodFramesChange = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSPECTION_DATA_DRONE_CHANGE))
        val framesPerSuper = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSPECTION_DATA_FRAMES_PER_SUPER))
        val supers = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSPECTION_DATA_SUPERS))
        val supplementedFeed = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSPECTION_DATA_SUPPLEMENTAL_FEED)) == 1
        val winterReady = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSPECTION_DATA_WINTER_READY)) == 1
        val aggressivity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSPECTION_DATA_AGGRESSIVITY))
        val honeyHarvested = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSPECTION_DATA_HONEY_HARVESTED))

        return InspectionData(
            id = id,
            hiveId = hiveId,
            noteId = noteId,
            broodFrames = broodFrames,
            broodFramesAdjusted = broodFramesAdjusted,
            broodFramesChange = broodFramesChange,
            honeyFrames = honeyFrames,
            honeyFramesAdjusted = honeyFramesAdjusted,
            honeyFramesChange = honeyFramesChange,
            freeSpaceFrames = freeSpaceFrames,
            droneBroodFrames = droneBroodFrames,
            droneBroodFramesAdjusted = droneBroodFramesAdjusted,
            droneBroodFramesChange = droneBroodFramesChange,
            framesPerSuper = framesPerSuper,
            supers = supers,
            supplementedFeed = supplementedFeed,
            winterReady = winterReady,
            aggressivity = aggressivity,
            honeyHarvested = honeyHarvested
        )
    }

    fun deleteInspection(dbHelper: DatabaseHelper, inspectionId: Int) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // Step 1: Get the inspectionDataIds string
            val cursor = db.rawQuery(
                "SELECT ${DatabaseHelper.COL_INSPECTION_DATA_IDS} FROM ${DatabaseHelper.TABLE_INSPECTIONS} WHERE ${DatabaseHelper.COL_INSPECTION_ID} = ?",
                arrayOf(inspectionId.toString())
            )

            if (cursor.moveToFirst()) {
                val dataIdsString = cursor.getString(0) ?: ""
                val idList = dataIdsString.split(",").mapNotNull { it.trim().toIntOrNull() }

                // Step 2: Delete associated inspection data
                for (id in idList) {
                    db.delete(
                        DatabaseHelper.TABLE_INSPECTION_DATA,
                        "${DatabaseHelper.COL_INSPECTION_DATA_ID} = ?",
                        arrayOf(id.toString())
                    )
                }
            }
            cursor.close()

            // Step 3: Delete the inspection itself
            val rowsDeleted = db.delete(
                DatabaseHelper.TABLE_INSPECTIONS,
                "${DatabaseHelper.COL_INSPECTION_ID} = ?",
                arrayOf(inspectionId.toString())
            )

            if (rowsDeleted > 0) {
                db.setTransactionSuccessful()
            } else {
                Log.e("DatabaseHelper", "Failed to delete inspection with ID $inspectionId")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("DatabaseHelper", "Error deleting inspection: ${e.message}")
        } finally {
            db.endTransaction()
            db.close()
        }
    }
}