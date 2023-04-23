package model

class IntervalFunction(
    private val transitionToFiringTime: MutableMap<Transition, FiringTimePair> = mutableMapOf<Transition, FiringTimePair>()
) {
    operator fun get(transition: Transition): FiringTimePair {
        return transitionToFiringTime[transition]!!
    }
}
