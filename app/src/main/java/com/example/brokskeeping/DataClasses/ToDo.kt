package com.example.brokskeeping.DataClasses

import java.util.Date

data class ToDo(
    val id: Int = -1,
    var hiveId: Int = -1,
    var notesId: Int = -1,
    var toDoText: String = "",
    var toDoState: Boolean = false,
    var date: Date = Date(0),
)
