package model

class Binding(
    private val transition: Transition,
) {

    fun execute() {
        for (inputArc in transition.inputArcs) {
            for (outputArc in transition.inputArcs) {
                val inputPlace = inputArc.requireTailPlace()
                val outputPlace = outputArc.requireArrowPlace()
                if (inputPlace.type == outputPlace.type) {
                    require(inputArc.isSameArcType(outputArc)) {
                        // TODO: enforce full consistency? -> let's suppose at this development stage
                        //  that the net is already consistent in a sense that there are no other arc for the same
                        //  object type
                        "arcs required to be of same type for one object type"
                    }
                    val toBeConsumed : Int
                    val toBeProduced : Int

                    when (inputArc) {
                       is NormalArc -> {
                            toBeConsumed = inputArc.multiplicity
                            toBeProduced = (outputArc as NormalArc).multiplicity
                       }
                        is VariableArc -> {
                            toBeConsumed = inputPlace.tokens
                            // TODO: here, can alter the amount of tokens that are produced based on the given
                            //  consumed amount of tokens
                            toBeProduced = toBeConsumed
                        }
                        else -> throw IllegalStateException("unrecognized type of arc: $inputArc")
                    }
                    inputPlace.consumeTokens(toBeConsumed)
                    outputPlace.addTokens(toBeProduced)
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
