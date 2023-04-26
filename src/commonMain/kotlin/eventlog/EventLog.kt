package eventlog

import model.ObjectToken
import model.ObjectType

typealias AttributeNames = List<String>
typealias ObjectTypes = MutableList<ObjectType>

class EventLog() {
    val events = mutableListOf<Event>()
    val objects: MutableList<ObjectToken> = mutableListOf()
    val objectTypes: ObjectTypes = mutableListOf()
    fun recordObjects(objects: Collection<ObjectToken>) {
        this.objects += objects
    }

    fun recordObjectTypes(objectTypes: Collection<ObjectType>) {
        this.objectTypes += objectTypes
    }

    fun recordEvent(event: Event) {
        events += event
    }
}
