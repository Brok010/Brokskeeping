package com.example.brokskeeping.DbFunctionality

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.example.brokskeeping.DataClasses.HoneyHarvest
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
        databaseHelper: DatabaseHelper,
        year: Int,
        month: Int,
        type: String
    ): Pair<List<Pair<Int, Int>>, Int> {
        val results = mutableListOf<Pair<Int, Int>>()

        // Compute the date range
        val calendar = Calendar.getInstance()
        val startTime: Long
        val endTime: Long

        try {
            when {
                year == 0 && month == 0 -> {
                    startTime = 0L
                    endTime = System.currentTimeMillis()
                }
                year > 0 && month == 0 -> {
                    calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0)
                    startTime = calendar.timeInMillis
                    calendar.set(year, Calendar.DECEMBER, 31, 23, 59, 59)
                    endTime = calendar.timeInMillis
                }
                year > 0 && month in 1..12 -> {
                    calendar.set(year, month - 1, 1, 0, 0, 0)
                    startTime = calendar.timeInMillis
                    calendar.set(year, month - 1, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
                    endTime = calendar.timeInMillis
                }
                else -> return Pair(emptyList(), 0)
            }

            when (type.lowercase()) {
                "station" -> {
                    // Get all stations
                    val stations = StationsFunctionality.getAllStations(databaseHelper)

                    for (station in stations) {
                        var totalHoneyFrames = 0
                        val hiveIds = StationsFunctionality.GetHiveIdsOfStation(databaseHelper, station.id)

                        // For each hive in the station
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
                                        if (date in startTime..endTime) {
                                            val honeyFrames = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HONEY_HARVESTS_HONEY_FRAMES))
                                            totalHoneyFrames += honeyFrames
                                        }
                                    }
                                }
                            }
                        }

                        // Add the total honey frames for the station
                        if (totalHoneyFrames > 0 ) {
                            results.add(Pair(station.id, totalHoneyFrames))
                        }
                    }
                }

                "hive" -> {
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
                                    if (date in startTime..endTime) {
                                        val honeyFrames = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HONEY_HARVESTS_HONEY_FRAMES))
                                        totalHoneyFrames += honeyFrames
                                    }
                                }
                            }
                        }
                        if (totalHoneyFrames > 0 ) {
                            results.add(Pair(hive.id, totalHoneyFrames))
                        }
                    }
                }
                else -> return Pair(emptyList(), 0)
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