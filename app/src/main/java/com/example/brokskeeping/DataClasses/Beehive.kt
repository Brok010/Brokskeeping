package com.example.brokskeeping.DataClasses

data class Beehive(
    val id: Int = -1,
    var stationId: Int = -1,
    var nameTag: String? = null,
    var qrTag: String? = null,
    var broodFrames: Int = -1,
    var honeyFrames: Int = -1,
    var droneBroodFrames: Int = -1,
    var framesPerSuper: Int = -1,
    var supers: Int = -1,
    var freeSpaceFrames: Int = -1,
    var colonyOrigin: String? = null, // string if external else hiveId  (as string)
    var colonyEndState: Int = -1, // 0 = dead, -1 = alive, else hiveId
    var supplementedFeedCount: Int = -1,
    var winterReady: Boolean = false,
    var aggressivity: Int = -1,
    var attentionWorth: Int = -1
)
