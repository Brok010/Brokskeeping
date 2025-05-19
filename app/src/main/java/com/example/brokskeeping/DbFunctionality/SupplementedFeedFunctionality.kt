package com.example.brokskeeping.DbFunctionality

import SupplementedFeed
import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.example.brokskeeping.Functionality.Utils
import java.util.Date

object SupplementedFeedFunctionality {

    fun saveSupplementedFeed(dbHelper: DatabaseHelper, feed: SupplementedFeed): Int {
        return try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(DatabaseHelper.COLUMN_HIVE_ID_FK_SUPPLEMENTED_FEED, feed.hiveId)
                put(DatabaseHelper.COLUMN_SUPPLEMENTED_FEED_DATE, feed.date.time) // Stored as UNIX timestamp
                put(DatabaseHelper.COLUMN_SUPPLEMENTED_FEED_KILOS, feed.kilos)
            }

            val result = db.insert(DatabaseHelper.TABLE_SUPPLEMENTED_FEED, null, values)
            if (result == -1L) 0 else 1
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    private fun createSupplementedFeedFromCursor(cursor: Cursor): SupplementedFeed {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUPPLEMENTED_FEED_ID))
        val hiveId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HIVE_ID_FK_SUPPLEMENTED_FEED))
        val dateLong = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUPPLEMENTED_FEED_DATE))
        val kilos = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUPPLEMENTED_FEED_KILOS))

        return SupplementedFeed(
            id = id,
            hiveId = hiveId,
            date = Date(dateLong),
            kilos = kilos
        )
    }

    fun getSupplementedFeedById(dbHelper: DatabaseHelper, id: Int): Pair<SupplementedFeed?, Int> {
        var feed: SupplementedFeed? = null
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                DatabaseHelper.TABLE_SUPPLEMENTED_FEED,
                null,
                "${DatabaseHelper.COLUMN_SUPPLEMENTED_FEED_ID} = ?",
                arrayOf(id.toString()),
                null, null, null
            )

            if (cursor.moveToFirst()) {
                feed = createSupplementedFeedFromCursor(cursor)
                cursor.close()
                Pair(feed, 1)
            } else {
                cursor.close()
                Pair(null, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(null, 0)
        }
    }

    fun getFilteredSupplementedFeed(
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
            when (type.lowercase()) {
                "station" -> {
                    val (stations, stationsResult) = StationsFunctionality.getAllStations(databaseHelper, 1)
                    if (stationsResult == 0) {
                        Log.e("SupplementedFeedFunctionality", "Station loading was not successful - stationFilter")
                    }

                    for (station in stations) {
                        var totalSupplementedFeed = 0
                        val hiveIds = StationsFunctionality.GetHiveIdsOfStation(databaseHelper, station.id)

                        for (hiveId in hiveIds) {
                            databaseHelper.readableDatabase.use { db ->
                                val query = """
                            SELECT ${DatabaseHelper.COLUMN_SUPPLEMENTED_FEED_KILOS}, ${DatabaseHelper.COLUMN_SUPPLEMENTED_FEED_DATE}
                            FROM ${DatabaseHelper.TABLE_SUPPLEMENTED_FEED}
                            WHERE ${DatabaseHelper.COLUMN_HIVE_ID_FK_SUPPLEMENTED_FEED} = ?
                        """.trimIndent()

                                db.rawQuery(query, arrayOf(hiveId.toString())).use { cursor ->
                                    while (cursor.moveToNext()) {
                                        val date = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUPPLEMENTED_FEED_DATE))
                                        if (date in startTime!!..endTime!!) {
                                            val honeyFrames = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUPPLEMENTED_FEED_KILOS))
                                            totalSupplementedFeed += honeyFrames
                                        }
                                    }
                                }
                            }
                        }

                        if (totalSupplementedFeed > 0) {
                            results.add(Pair(station.id, totalSupplementedFeed))
                        }
                    }
                }

                "hive" -> {
                    val (hives, result) = HivesFunctionality.getAllHives(databaseHelper, dead = 0)
                    if (result == 0) {
                        Log.e("HoneyHarvestFunctionality", "getAllHives function did not finish properly")
                    }

                    for (hive in hives) {
                        var totalSupplementedFeed = 0
                        databaseHelper.readableDatabase.use { db ->
                            val query = """
                        SELECT ${DatabaseHelper.COLUMN_SUPPLEMENTED_FEED_KILOS}, ${DatabaseHelper.COLUMN_SUPPLEMENTED_FEED_DATE}
                        FROM ${DatabaseHelper.TABLE_SUPPLEMENTED_FEED}
                        WHERE ${DatabaseHelper.COLUMN_HIVE_ID_FK_SUPPLEMENTED_FEED} = ?
                    """.trimIndent()

                            db.rawQuery(query, arrayOf(hive.id.toString())).use { cursor ->
                                while (cursor.moveToNext()) {
                                    val date = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUPPLEMENTED_FEED_DATE))
                                    if (date in startTime!!..endTime!!) {
                                        val honeyFrames = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUPPLEMENTED_FEED_KILOS))
                                        totalSupplementedFeed += honeyFrames
                                    }
                                }
                            }
                        }
                        if (totalSupplementedFeed > 0) {
                            results.add(Pair(hive.id, totalSupplementedFeed))
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
}
