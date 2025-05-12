package com.example.brokskeeping.DataClasses

import java.util.Date

data class HiveNotes(
    var id: Int = -1,
    var stationId: Int = -1,
    var hiveId: Int = -1,
    var noteText: String = "",
    var noteDate: Date = Date(0)
)
