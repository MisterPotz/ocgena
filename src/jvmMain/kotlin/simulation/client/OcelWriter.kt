package simulation.client

import eventlog.Event
import eventlog.EventLog
import model.ObjectToken

actual class OcelWriter {
    //    fun writeObjectType(objectType: ObjectType)
    actual fun writeEvent(event: Event) {
    }

    actual fun writeObjectToken(objectToken: ObjectToken) {
    }

    actual fun write(eventLog: EventLog) {
    }

    actual fun collect(): Any {
        TODO("Not yet implemented")
    }
}
