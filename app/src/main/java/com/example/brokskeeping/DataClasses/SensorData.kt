package com.example.brokskeeping.DataClasses

import java.util.Date

data class SensorData(
    val date: Date = Date(0),
    val temp: Double = Double.MIN_VALUE,
    val hum: Double = Double.MIN_VALUE
)
