package model

import ru.misterpotz.model.marking.Time

interface TransitionInstancesList {
    fun add(tMarkingValue: TransitionInstance)
    fun shiftAllValuesByTime(time: Time)
    fun getWithEarliestFinishTime(): TransitionInstance
    fun removeFinishedTransitions(): List<TransitionInstance>
    fun isEmpty(): Boolean
    fun iterable() : Iterable<TransitionInstance>
}

fun TransitionInstancesList(ongoingActivities: MutableList<TransitionInstance> = mutableListOf()): TransitionInstancesList {
    return TransitionInstancesListImpl(ongoingActivities)
}

internal class TransitionInstancesListImpl(
    private val tMarkingValues: MutableList<TransitionInstance> = mutableListOf()
) : TransitionInstancesList {

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

    override fun toString(): String {
        return tMarkingValues.joinToString(separator = "\n") { it.toString() }
    }

    override fun add(tMarkingValue: TransitionInstance) {
        tMarkingValues.add(tMarkingValue)
    }

    override fun shiftAllValuesByTime(time: Time) {
        for (i in tMarkingValues.indices) {
            tMarkingValues[i] = with(tMarkingValues[i]) {
                copy(
                    relativeTimePassedSinceLock = relativeTimePassedSinceLock + time
                )
            }
        }
    }

    override fun getWithEarliestFinishTime(): TransitionInstance {
        return tMarkingValues.minBy { it.timeLeftUntilFinish() }
    }

    override fun removeFinishedTransitions(): List<TransitionInstance> {
        val mutableList = mutableListOf<TransitionInstance>()
        for (i in tMarkingValues.indices.reversed()) {
            val shouldFinish = tMarkingValues[i].timeLeftUntilFinish() <= 0
            if (shouldFinish) {
                mutableList.add(tMarkingValues[i])
                tMarkingValues.removeAt(i)
            }
        }
        return mutableList
    }

    override fun isEmpty(): Boolean {
        return tMarkingValues.isEmpty()
    }

    override fun iterable(): Iterable<TransitionInstance> {
        return tMarkingValues.asIterable()
    }

    fun getEndedTransitions(): List<TransitionInstance> {
        return tMarkingValues.filter { it.timeLeftUntilFinish() <= 0 }
    }
}
