package model

import kotlinx.serialization.Serializable
import ru.misterpotz.model.marking.Time

@Serializable
class SerializableOngoingActivitiesList(val tMarkingValues: List<SerializableActiveFiringTransition>)


interface OngoingActivitiesList {
    fun add(tMarkingValue: OngoingActivity)
    fun shiftAllValuesByTime(time: Time)
    fun getWithEarliestFinishTime(): OngoingActivity
    fun removeFinishedTransitions(): List<OngoingActivity>
    fun get
    fun isEmpty(): Boolean
}

fun OngoingActivitiesList(ongoingActivities: MutableList<OngoingActivity> = mutableListOf()): OngoingActivitiesList {
    return OngoingActivitiesListImpl(ongoingActivities)
}

internal class OngoingActivitiesListImpl(
    private val tMarkingValues: MutableList<OngoingActivity> = mutableListOf()
) : OngoingActivitiesList {

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

    fun toSerializable(): SerializableOngoingActivitiesList {
        return SerializableOngoingActivitiesList(tMarkingValues.map { it.toSerializable() })
    }

    fun prettyPrintState(): String {
        return tMarkingValues.joinToString(separator = "\n") { it.prettyPrintState() }
    }

    override fun toString(): String {
        return tMarkingValues.joinToString(separator = "\n") { it.toString() }
    }

    override fun add(tMarkingValue: OngoingActivity) {
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
    override fun getWithEarliestFinishTime(): OngoingActivity {
        return tMarkingValues.minBy { it.timeLeftUntilFinish() }
    }

    override fun removeFinishedTransitions(): List<OngoingActivity> {
        val mutableList = mutableListOf<OngoingActivity>()
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

    fun getEndedTransitions(): List<OngoingActivity> {
        return tMarkingValues.filter { it.timeLeftUntilFinish() <= 0 }
    }
}
