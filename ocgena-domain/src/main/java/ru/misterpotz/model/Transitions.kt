package model

class Transitions(val transitions: List<Transition>) : List<Transition> by transitions {
    operator fun get(transitionId : String): Transition {
        return transitions.find { it.id == transitionId }!!
    }

    fun reindexArcs() {
        for (transition in transitions) {
            transition.reindexArcs()
        }
    }
}
