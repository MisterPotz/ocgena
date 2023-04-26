package eventlog

import model.Activity

data class Event(
    val activity: Activity,
    val timestamp: Timestamp,
    val oMap: ObjectsLists,
    val vMap: ValuesMap,
) {
}
