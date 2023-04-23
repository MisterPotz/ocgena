package model

class LabelsActivities(val transitionsToActivity: MutableMap<Transition, Activity>) {
    operator fun get(transition: Transition): Activity {
        return transitionsToActivity[transition]!!
    }

    companion object {
        fun createFromTransitions(transitions: Transitions) : LabelsActivities {
            return LabelsActivities(
                transitionsToActivity = transitions.fold(mutableMapOf()) { accum, transition ->
                    accum[transition] = transition.label
                    return@fold accum
                },
            )
        }
    }
}
