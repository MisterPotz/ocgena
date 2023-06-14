package eventlog

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import model.Activity

@Serializable
data class Event(
    val eventId : String,
    val activity: Activity,
    val timestamp: Timestamp,
    val oMap: ObjectsLists,
    @Transient
    val vMap: ValuesMap = mapOf(),
) {
}
