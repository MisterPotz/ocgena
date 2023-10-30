package simulation.client

import eventlog.Event
import ru.misterpotz.model.marking.ObjectToken
import model.ObjectType

interface OcelWriter {
    fun writeEvent(event: Event)
    fun writeObjectToken(objectToken: ObjectToken)
    fun writeObjectType(objectType : ObjectType)
}

class DebugOcelWriter() : OcelWriter {
    val json =
    override fun writeEvent(event: Event) {
        TODO("Not yet implemented")
    }

    override fun writeObjectToken(objectToken: ObjectToken) {
        TODO("Not yet implemented")
    }

    override fun writeObjectType(objectType: ObjectType) {
        TODO("Not yet implemented")
    }
}