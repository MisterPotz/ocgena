package ru.misterpotz.ocgena.simulation_old.eventlog

import ru.misterpotz.ocgena.simulation_old.ObjectToken
import ru.misterpotz.ocgena.simulation_old.ObjectType

typealias ObjectTypes = MutableList<ObjectType>


@Deprecated("collecting everything into runtime memory is extremely bad idea. Think twice.")
class EventLog() {
    val events = mutableListOf<Event>()
    val objects: MutableSet<ObjectToken> = mutableSetOf()

    val objectTypes: ObjectTypes = mutableListOf()

    fun recordObjectTypes(objectTypes: Collection<ObjectType>) {
        this.objectTypes += objectTypes
    }

    fun recordEvent(event: Event) {
        events += event
    }
}
