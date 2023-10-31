package model

import kotlinx.serialization.Serializable
import ru.misterpotz.model.marking.Time
import kotlin.math.min


@Serializable
data class SerializableActiveTransitionMarking(
    val transitionsToTMarkingValue: Map<TransitionId, SerializableOngoingActivitiesList>
)

class TransitionActivitiesMarking() {
    private val transitionsToTMarkingValue = mutableMapOf<TransitionId, OngoingActivitiesList>()

    operator fun get(transitionId: TransitionId): OngoingActivitiesList? {
        return transitionsToTMarkingValue[transitionId]
    }

    fun shiftByTime(time: Time) {
        transitionsToTMarkingValue.forEach { entry ->
            entry.value.shiftAllValuesByTime(time)
        }
    }

    fun getActiveTransitionWithEarliestFinish(): OngoingActivity? {
        return transitionsToTMarkingValue
            .values
            .fold<OngoingActivitiesList, OngoingActivity?>(null) { currentMin, ongoingActivitiesList ->
                val activityCandidate = ongoingActivitiesList.getWithEarliestFinishTime()
                val earliestFinishTime = activityCandidate.timeLeftUntilFinish()
                val minFinishTime = currentMin?.timeLeftUntilFinish()

                return if (minFinishTime == null || minFinishTime > earliestFinishTime) {
                    activityCandidate
                } else {
                    currentMin
                }
            }
    }
    
    fun getAndPopEndedTransitions(): Collection<OngoingActivity> {
        val mutableList = mutableListOf<OngoingActivity>()
        for (key in transitionsToTMarkingValue.keys) {
            val value = transitionsToTMarkingValue[key]!!
            val endedTransitions = value.removeFinishedTransitions()
            mutableList.addAll(endedTransitions)
            if (value.isEmpty()) {
                transitionsToTMarkingValue.remove(key)
            }
        }
        return mutableList
    }

    fun pushTMarking(ongoingActivity: OngoingActivity) {
        val transition = ongoingActivity.transition
        val current = transitionsToTMarkingValue.getOrPut(transition) {
            OngoingActivitiesListImpl()
        }
        current.add(ongoingActivity)
    }
}

