package simulation.event

import model.Activity
import simulation.ObjectsMap
import simulation.Timestamp
import simulation.ValuesMap

interface Event {
    val activity: Activity
    val timestamp: Timestamp
    val oMap: ObjectsMap
    val vMap: ValuesMap
}
