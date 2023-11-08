package eventlog

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ru.misterpotz.ocgena.eventlog.ObjectsLists
import ru.misterpotz.ocgena.eventlog.Timestamp
import ru.misterpotz.ocgena.eventlog.ValuesMap

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
