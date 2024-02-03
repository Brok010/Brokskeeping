package com.example.brokskeeping.DataClasses

import java.util.Date

data class HumTempData(
    val id: Int = -1,
    var stationId: Int = -1,
    var hiveId: Int = -1,
    var logText: String = "",
    var firstDate: Date = Date(0),
    var lastDate: Date = Date(0)
)
