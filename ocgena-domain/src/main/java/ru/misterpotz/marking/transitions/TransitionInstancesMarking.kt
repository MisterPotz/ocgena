package ru.misterpotz.marking.transitions

import model.TransitionId
import ru.misterpotz.marking.objects.Time

class TransitionInstancesMarking() {
    private val transitionsToTMarkingValue = mutableMapOf<TransitionId, TransitionInstancesList>()

    operator fun get(transitionId: TransitionId): TransitionInstancesList? {
        return transitionsToTMarkingValue[transitionId]
    }

    fun shiftByTime(time: Time) {
        transitionsToTMarkingValue.forEach { entry ->
            entry.value.shiftAllValuesByTime(time)
        }
    }

    fun getActiveTransitionWithEarliestFinish(): TransitionInstance? {
        return transitionsToTMarkingValue
            .values
            .fold<TransitionInstancesList, TransitionInstance?>(null) { currentMin, ongoingActivitiesList ->
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

    fun getAndPopEndedTransitions(): Collection<TransitionInstance> {
        val mutableList = mutableListOf<TransitionInstance>()
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

    fun pushTMarking(transitionInstance: TransitionInstance) {
        val transition = transitionInstance.transition
        val current = transitionsToTMarkingValue.getOrPut(transition) {
            TransitionInstancesList()
        }
        current.add(transitionInstance)
    }
}
