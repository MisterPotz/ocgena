package model

class ActiveFiringTransitions {
    private val tMarkingValues: MutableList<ActiveFiringTransition> = mutableListOf()

    fun checkConsistency() {
        for (i in 1 until tMarkingValues.indices.last) {
            val earlierTMarking = tMarkingValues[i - 1]
            val laterTMarking = tMarkingValues[i]
            require(
                earlierTMarking.relativeTimePassedSinceLock
                        > laterTMarking.relativeTimePassedSinceLock
            )
        }
    }

    fun add(tMarkingValue: ActiveFiringTransition) {
        tMarkingValues.add(tMarkingValue)
    }

    fun pop(): ActiveFiringTransition {
        return tMarkingValues.first().also {
            tMarkingValues.removeAt(0)
        }
    }

    fun getWithEarliestFinishTime() : ActiveFiringTransition {
        return tMarkingValues.minBy {  it.timeLeftUntilFinish() }
    }

    fun shiftByTime(time: Time) {
        for (i in tMarkingValues.indices) {
            tMarkingValues[i] = with(tMarkingValues[i]){
                copy(
                    relativeTimePassedSinceLock = relativeTimePassedSinceLock + time
                )
            }
        }
    }

    fun getEndedTransitions() : List<ActiveFiringTransition> {
        return tMarkingValues.filter { it.timeLeftUntilFinish() <= 0 }
    }
}
