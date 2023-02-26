package model

class Binding(
    val transition: Transition,
) {

    fun execute() {
        val inputPlaces = transition.inputPlaces
        val outputPlaces = transition.outputPlaces
        for (inputArc in transition.inputArcs) {
            for (outputArc in transition.inputArcs) {
                val inputPlace = inputArc.requireTailPlace()
                val outputPlace = outputArc.requireArrowPlace()
                if (inputPlace.type == outputPlace.type) {
                    require(inputArc.isSameArcType(outputArc)) {
                        // TODO: enforce full consistency? -> let's suppose at this development stage
                        //  that the net is already consistent in a sense that there are no other arc for the same
                        //  object type
                        "arcs required to be of same type for the same object type"
                    }
                    val toBeConsumed = inputArc.multiplicity
                    val toBeProduced = outputArc.multiplicity
                    inputPlace.
                }

            }
        }
        for (inputPlace in inputPlaces) {
            for (outputPlace in outputPlaces) {
                if (inputPlace.type == outputPlace.type) {

                }
            }
        }
    }

    companion object {
        fun createEnabledBinding(
            transition: Transition
        ) : Binding? {
            val bindingEnabled = transition.isBindingEnabled()
            if (!bindingEnabled) return null
            return Binding(transition)
        }
    }
}
