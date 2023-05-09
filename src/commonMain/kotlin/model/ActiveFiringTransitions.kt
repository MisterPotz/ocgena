package model

class ActiveFiringTransitions() {
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

    fun prettyPrintState(): String {
        return tMarkingValues.joinToString(separator = "\n") { it.prettyPrintState() }
    }

    fun htmlLines() : List<String> {
        return tMarkingValues.flatMap { it.prettyPrintHtmlLinesState() }
    }

    override fun toString(): String {
        return tMarkingValues.joinToString(separator = "\n") { it.toString() }
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

    fun getAndPopEndedTransitions() : List<ActiveFiringTransition> {
        val mutableList = mutableListOf<ActiveFiringTransition>()
        for (i in tMarkingValues.indices.reversed()) {
            val weTakeThis = tMarkingValues[i].timeLeftUntilFinish() <= 0
            if (weTakeThis) {
                mutableList.add(tMarkingValues[i])
                tMarkingValues.removeAt(i)
            }
        }
        return mutableList
    }

    fun hasTransitions() : Boolean {
        return tMarkingValues.isNotEmpty()
    }

    fun getEndedTransitions() : List<ActiveFiringTransition> {
        return tMarkingValues.filter { it.timeLeftUntilFinish() <= 0 }
    }
}
