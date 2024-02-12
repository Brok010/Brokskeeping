package com.example.brokskeeping.DataClasses

import java.util.Date

data class ToDo(
    val id: Int = -1,
    var hiveId: Int = -1,
    var toDoText: String = "",
    var toDoState: Boolean = false, //1 done, 0 undone
    var date: Date = Date(0),
)
