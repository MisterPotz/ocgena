package model.time

import model.Transition
import model.TransitionId

class IntervalFunction(
    private val defaultTransitionTimes: TransitionTimes?,
    private val transitionToFiringTime: MutableMap<TransitionId, TransitionTimes> = mutableMapOf(),
) {
    operator fun get(transition: Transition): TransitionTimes {
        return transitionToFiringTime[transition.id] ?: defaultTransitionTimes!!
    }

    companion object {
        fun create(
            defaultTransitionTimes: TransitionTimes? = null,
            block: MutableMap<TransitionId, TransitionTimes>.() -> Unit): IntervalFunction {
            return IntervalFunction(
                defaultTransitionTimes = defaultTransitionTimes,
                buildMap {
                    block()
                }.toList().fold(mutableMapOf()) { accum, entry ->
                    accum[entry.first] = entry.second
                    accum
                }
            )
        }
    }
}
