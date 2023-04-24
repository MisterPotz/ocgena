package model

class IntervalFunction(
    private val transitionToFiringTime: MutableMap<Transition, FiringTimePair> = mutableMapOf<Transition, FiringTimePair>(),
) {
    operator fun get(transition: Transition): FiringTimePair {
        return transitionToFiringTime[transition]!!
    }

    companion object {
        fun create(block: MutableMap<Transition, FiringTimePair>.() -> Unit): IntervalFunction {
            return IntervalFunction(
                buildMap {
                    block()
                }.toMutableMap()
            )
        }
    }
}
