package model

class Binding(
    private val transition: Transition,
) {

    var executed : Boolean = false
        private set

    var bindingIndex : Int = UNSET_BINDING_INDEX
        private set
    var loggingEnabled : Boolean = false
        private set

    private val consumedMap : MutableMap<String, Int> = mutableMapOf()
    private val producedMap : MutableMap<String, Int> = mutableMapOf()

    fun execute(
        bindingIndex : Int,
        loggingEnabled : Boolean
    ) {
        // TODO: fix how consumed / produced are calculated
        require(!executed)
        this.bindingIndex = bindingIndex
        this.loggingEnabled = loggingEnabled
        for (inputArc in transition.inputArcs) {
            for (outputArc in transition.outputArcs) {
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
                    consumedMap[inputPlace.label] = toBeConsumed
                    inputPlace.consumeTokens(toBeConsumed)
                    producedMap[outputPlace.label] = toBeProduced
                    outputPlace.addTokens(toBeProduced)
                }
            }
        }
        executed = true
    }

    private fun logPlaceLabelMap(
        map: MutableMap<String, Int>
    ) : String {
        return map.map { (key, value) ->
            "$key ${value}"
        }.joinToString(", ")
    }

    override fun toString(): String {
        return """
            Binding(
                index: ${bindingIndex.takeIf { it != UNSET_BINDING_INDEX } ?: "UNSET"} 
                executed: $executed
                transition:  $transition
                ${if (loggingEnabled) { 
                    "consumed map: [ ${logPlaceLabelMap(consumedMap)} ]" 
                } else  {
                    "logging disabled"
                }}
                ${if (loggingEnabled) { "produced map: [ ${logPlaceLabelMap(producedMap)} ]" } else "" }
            )
        """.trimIndent()
    }

    companion object {
        const val UNSET_BINDING_INDEX = -1
        fun createEnabledBinding(
            transition: Transition
        ) : Binding? {
            val bindingEnabled = transition.isBindingEnabled()
            if (!bindingEnabled) return null
            return Binding(transition)
        }
    }
}
