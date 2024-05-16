package ru.misterpotz.ocgena.simulation_old.eventlog

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Event(
    val eventId : String,
    val activity: String,
    val timestamp: Timestamp,
    val oMap: ObjectsLists,
    @Transient
    val vMap: ValuesMap = mapOf(),
) : java.io.Serializable {
}
