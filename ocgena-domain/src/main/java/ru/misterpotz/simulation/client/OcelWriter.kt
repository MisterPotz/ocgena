package simulation.client

import eventlog.Event
import eventlog.EventLog
import model.ObjectToken


expect class OcelWriter {
    fun collect() : Any
    fun write(eventLog: EventLog)
    fun writeEvent(event: Event)
    fun writeObjectToken(objectToken: ObjectToken)
//    fun writeObjectType(objectType: ObjectType)
}
