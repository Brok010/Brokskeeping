package com.example.brokskeeping.DataClasses

data class MaxMins(
    val maxTemp: Double = Double.MAX_VALUE,
    val minTemp: Double = Double.MIN_VALUE,
    val maxHum: Double = Double.MAX_VALUE,
    val minHum: Double = Double.MIN_VALUE
)
