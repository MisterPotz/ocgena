package eventlog

import simulation.client.OcelParams


class ActivityOccurrenceRegistry(
    private val ocelParams: OcelParams
){
    private var activityOccurrenceRegistry: MutableMap<String, Int> = mutableMapOf()

    fun onStartOccurrencesOf(activityId : String) : Int {
        val times = activityOccurrenceRegistry.getOrPut(activityId) { 0 }
        val updTimes = times + 1
        activityOccurrenceRegistry[activityId] = updTimes

        return times
    }

    fun onEndOccurrencesOf(activityId: String) : Int {
        val times = activityOccurrenceRegistry.getOrPut(activityId) { 0 }
        val updTimes = if (ocelParams.logBothStartAndEnd) {
            times - 1
        } else {
            times
        }
        val toPut = updTimes + 1
        activityOccurrenceRegistry[activityId] = toPut
        return updTimes
    }
}

