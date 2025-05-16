package com.example.brokskeeping.DataClasses

data class InspectionData(
    val id: Int = -1,
    val inspectionId: Int = -1,
    val hiveId: Int = -1,
    val noteId: Int = -1,
    var broodFrames: Int = -1,
    var broodFramesAdjusted: Boolean = false,
    var broodFramesChange: Int = -1,
    var honeyFrames: Int = -1,
    var honeyFramesAdjusted: Boolean = false,
    var honeyFramesChange: Int = -1,
    var freeSpaceFrames: Int = -1,
    var droneBroodFrames: Int = -1,
    var droneBroodFramesAdjusted: Boolean = false,
    var droneBroodFramesChange: Int = -1,
    var framesPerSuper: Int = -1,
    var supers: Int = -1,
    var supplementedFeed: Boolean = false,
    var winterReady: Boolean = false,
    var aggressivity: Int = -1,
    var honeyHarvested: Int = -1,
    var attentionWorth: Int = -1,
    var colonyEndState: Int = -1, // 0 = dead, -1 = alive, else hiveId
    var separated: Int = -1, // -1 not, else id of new hive
    var joined: Int = -1 // -1 not, else id of the joined hive
)
