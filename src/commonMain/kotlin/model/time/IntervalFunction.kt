package model.time

import model.Transition

class IntervalFunction(
    private val transitionToFiringTime: MutableMap<Transition, TransitionTimes> = mutableMapOf<Transition, TransitionTimes>(),
) {
    operator fun get(transition: Transition): TransitionTimes {
        return transitionToFiringTime[transition]!!
    }

    companion object {
        fun create(block: MutableMap<Transition, TransitionTimes>.() -> Unit): IntervalFunction {
            return IntervalFunction(
                buildMap {
                    block()
                }.toMutableMap()
            )
        }
    }
}
