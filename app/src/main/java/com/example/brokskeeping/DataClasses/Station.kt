package com.example.brokskeeping.DataClasses

import java.util.Date

data class Station(
    var id :Int = -1,
    var name :String = "",
    var location: String= "",
    var inUse: Int = -1, //0 no 1 yes?
    var creationTime: Date = Date(0),
    var deathTime: Date = Date(0)
)
