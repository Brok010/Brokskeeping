package com.example.brokskeeping.DbFunctionality

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.example.brokskeeping.DataClasses.HoneyHarvest
import com.example.brokskeeping.Functionality.Utils
import com.example.brokskeeping.R
import java.util.Calendar
import java.util.Date

object HoneyHarvestFunctionality {
    fun saveHoneyHarvest(databaseHelper: DatabaseHelper, honeyHarvest: HoneyHarvest): Int {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_HIVE_ID_FK_HONEY_HARVEST, honeyHarvest.hiveId)
            put(DatabaseHelper.COLUMN_HONEY_HARVESTS_DATE, honeyHarvest.date.time) // Store as timestamp
            put(DatabaseHelper.COLUMN_HONEY_HARVESTS_HONEY_FRAMES, honeyHarvest.honeyFrames)
        }

        val newRowId = db.insert(DatabaseHelper.TABLE_HONEY_HARVESTS, null, values)
        db.close()

        return newRowId.toInt()
    }

    fun getFilteredHoneyHarvests(
        context: Context,
        databaseHelper: DatabaseHelper,
        year: Int? = null,
        month: Int? = null,
        type: String
    ): Pair<List<Pair<Int, Int>>, Int> {
        val results = mutableListOf<Pair<Int, Int>>()

        val (times, timesResult) = Utils.getStartAndEndTime(year, month)
        if (timesResult == 0) return emptyList<Pair<Int, Int>>() to 0
        val (startTime, endTime) = times

        try {
            when (type) {
                context.getString(R.string.station) -> {
                val (stations, stationsResult) = StationsFunctionality.getAllStations(databaseHelper, 1)
                if (stationsResult == 0) {
                    Log.e("HoneyHarvestFunctionality", "Station loading was not successful - stationFilter")
                }

                for (station in stations) {
                    var totalHoneyFrames = 0
                    val hiveIds = StationsFunctionality.GetHiveIdsOfStation(databaseHelper, station.id)

                    for (hiveId in hiveIds) {
                        databaseHelper.readableDatabase.use { db ->
                            val query = """
                            SELECT ${DatabaseHelper.COLUMN_HONEY_HARVESTS_HONEY_FRAMES}, ${DatabaseHelper.COLUMN_HONEY_HARVESTS_DATE}
                            FROM ${DatabaseHelper.TABLE_HONEY_HARVESTS}
                            WHERE ${DatabaseHelper.COLUMN_HIVE_ID_FK_HONEY_HARVEST} = ?
                        """.trimIndent()

                            db.rawQuery(query, arrayOf(hiveId.toString())).use { cursor ->
                                while (cursor.moveToNext()) {
                                    val date = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HONEY_HARVESTS_DATE))
                                    if (date in startTime!!..endTime!!) {
                                        val honeyFrames = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HONEY_HARVESTS_HONEY_FRAMES))
                                        totalHoneyFrames += honeyFrames
                                    }
                                }
                            }
                        }
                    }

                    if (totalHoneyFrames > 0) {
                        results.add(Pair(station.id, totalHoneyFrames))
                    }
                }
            }

                context.getString(R.string.hive) -> {
                val (hives, result) = HivesFunctionality.getAllHives(databaseHelper, dead = 0)
                if (result == 0) {
                    Log.e("HoneyHarvestFunctionality", "getAllHives function did not finish properly")
                }

                for (hive in hives) {
                    var totalHoneyFrames = 0
                    databaseHelper.readableDatabase.use { db ->
                        val query = """
                        SELECT ${DatabaseHelper.COLUMN_HONEY_HARVESTS_HONEY_FRAMES}, ${DatabaseHelper.COLUMN_HONEY_HARVESTS_DATE}
                        FROM ${DatabaseHelper.TABLE_HONEY_HARVESTS}
                        WHERE ${DatabaseHelper.COLUMN_HIVE_ID_FK_HONEY_HARVEST} = ?
                    """.trimIndent()

                        db.rawQuery(query, arrayOf(hive.id.toString())).use { cursor ->
                            while (cursor.moveToNext()) {
                                val date = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HONEY_HARVESTS_DATE))
                                if (date in startTime!!..endTime!!) {
                                    val honeyFrames = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HONEY_HARVESTS_HONEY_FRAMES))
                                    totalHoneyFrames += honeyFrames
                                }
                            }
                        }
                    }
                    if (totalHoneyFrames > 0) {
                        results.add(Pair(hive.id, totalHoneyFrames))
                    }
                }
            }
            else -> {
                // If the type is not "station" or "hive", return an empty result
                return Pair(emptyList(), 0)
            }
        }

            return Pair(results, 1)
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair(emptyList(), 0)
        }
    }



    private fun createHoneyHarvestFromCursor(cursor: Cursor): HoneyHarvest {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HONEY_HARVESTS_ID))
        val hiveId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HIVE_ID_FK_HONEY_HARVEST))
        val dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HONEY_HARVESTS_DATE))
        val honeyFrames = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HONEY_HARVESTS_HONEY_FRAMES))

        return HoneyHarvest(
            id = id,
            hiveId = hiveId,
            date = Date(dateMillis),
            honeyFrames = honeyFrames
        )
    }


}