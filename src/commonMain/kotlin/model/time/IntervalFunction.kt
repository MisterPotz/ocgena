package model.time

import model.Transition
import model.TransitionId

class IntervalFunction(
    private val transitionToFiringTime: MutableMap<TransitionId, TransitionTimes> = mutableMapOf(),
) {
    operator fun get(transition: Transition): TransitionTimes {
        return transitionToFiringTime[transition.id]!!
    }

    companion object {
        fun create(block: MutableMap<Transition, TransitionTimes>.() -> Unit): IntervalFunction {
            return IntervalFunction(
                buildMap {
                    block()
                }.toList().fold(mutableMapOf()) { accum, entry ->
                    accum[entry.first.id] = entry.second
                    accum
                }
            )
        }
    }
}
