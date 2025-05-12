package com.example.brokskeeping.DataClasses

import java.util.Date

data class History(
    val id: Int = -1,
    var entityId: Int = -1,
    var entityType: String? = null,
    var date: Date = Date(0),
    var event: String? = null
)
