package com.example.brokskeeping.DataClasses

import java.util.Date

data class HoneyHarvest(
    val id: Int = -1,
    var hiveId: Int = -1,
    var date: Date = Date(0),
    var honeyFrames: Int = -1
)
