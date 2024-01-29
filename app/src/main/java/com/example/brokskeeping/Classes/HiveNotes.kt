package com.example.brokskeeping.Classes

import java.util.Date

data class HiveNotes(
    val id: Int = -1,
    var stationId: Int = -1,
    var hiveId: Int = -1,
    var notesText: String = "",
    var date: Date = Date(0),
)
