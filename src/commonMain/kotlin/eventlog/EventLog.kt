package eventlog

import model.ObjectToken
import model.ObjectType
import simulation.client.OcelWriter
import simulation.client.Writer

typealias AttributeNames = List<String>
typealias ObjectTypes = MutableList<ObjectType>

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
