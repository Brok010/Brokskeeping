package com.example.brokskeeping.DataClasses

sealed class DiscardedEntity {
    data class DiscardedHive(val hive: Beehive) : DiscardedEntity()
    data class DiscardedStation(val station: Station) : DiscardedEntity()
}
