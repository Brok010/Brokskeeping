package com.example.brokskeeping.DataClasses

import java.util.Date

data class Inspection(
    val id: Int = -1,
    val stationId: Int = -1,
    var finished: Boolean = false,
    var date: Date = Date(0),
    var inspectionDataIds: List<Int>
)
