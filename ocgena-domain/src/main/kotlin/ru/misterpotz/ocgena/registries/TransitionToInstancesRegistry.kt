package ru.misterpotz.ocgena.registries

import ru.misterpotz.ocgena.collections.TransitionInstance
import ru.misterpotz.ocgena.collections.TransitionInstancesList
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.Time

class TransitionToInstancesRegistry {
    private val transitionsToTMarkingValue = mutableMapOf<PetriAtomId, TransitionInstancesList>()

    operator fun get(transitionId: PetriAtomId): TransitionInstancesList? {
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
