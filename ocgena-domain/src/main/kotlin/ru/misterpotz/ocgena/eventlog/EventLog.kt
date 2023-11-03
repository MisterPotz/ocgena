package ru.misterpotz.ocgena.eventlog

import eventlog.Event
import ru.misterpotz.ocgena.simulation.ObjectToken
import ru.misterpotz.ocgena.simulation.ObjectType

typealias ObjectTypes = MutableList<ObjectType>


@Deprecated("collecting everything into runtime memory is extremely bad idea. Think twice.")
class EventLog() {
    val events = mutableListOf<Event>()
    val objects: MutableSet<ObjectToken> = mutableSetOf()

    val objectTypes: ObjectTypes = mutableListOf()
    fun recordObjects(objects: Collection<ObjectToken>) {
        for (i in objects) {
            val type = i.type
            objectTypes.add(type)
        }

        this.objects += objects
    }

    fun recordObjectTypes(objectTypes: Collection<ObjectType>) {
        this.objectTypes += objectTypes
    }

    fun recordEvent(event: Event) {
        events += event
    }
}
