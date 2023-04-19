package model

class ExecutedBinding(
    private val transition: Transition,
    private val consumedMap : MutableMap<String, Int>,
    private val producedMap : MutableMap<String, Int>
)

interface BindingExecutor(
    fun executeBinding
)

class ActiveBinding(
    private val transition: Transition,
) {
    var bindingIndex: Int = UNSET_BINDING_INDEX
        private set
    var loggingEnabled: Boolean = false
        private set

    private val consumedMap: MutableMap<String, Int> = mutableMapOf()
    private val producedMap: MutableMap<String, Int> = mutableMapOf()

    // precondition - this binding is enabled
    fun execute(
        bindingIndex: Int,
        loggingEnabled: Boolean,
    ) {
        require(transition.isBindingEnabled())

        this.bindingIndex = bindingIndex
        this.loggingEnabled = loggingEnabled
        val typeVariableConsumed = mutableMapOf<ObjectType, Int>()

        for (inputArc in transition.inputArcs) {
            val tailPlace = inputArc.requireTailPlace()
            when (inputArc) {
                is VariableArc -> {
                    val consumedTokens = tailPlace.consumeAllTokens()
                    consumedMap[tailPlace.label] = consumedTokens
                    typeVariableConsumed[tailPlace.type] = consumedTokens
                }

                is NormalArc -> {
                    val consumedTokens = inputArc.multiplicity
                    consumedMap[tailPlace.label] = consumedTokens
                    tailPlace.consumeTokens(consumedTokens)
                }
            }
        }
        for (outputArc in transition.outputArcs) {
            val arrowPlace = outputArc.requireArrowPlace()
            when (outputArc) {
                is VariableArc -> {
                    val producedTokens = typeVariableConsumed[arrowPlace.type]
                    producedMap[arrowPlace.label] = producedTokens ?: 0
                    if (producedTokens == 0 || producedTokens == null) continue
                    arrowPlace.addTokens(producedTokens)
                }

                is NormalArc -> {
                    val producedTokens = outputArc.multiplicity
                    producedMap[arrowPlace.label] = producedTokens
                    arrowPlace.addTokens(producedTokens)
                }
            }
        }
        return ExecutedBinding(
            transition,
            consumedMap,
            producedMap
        )
    }

    private fun logPlaceLabelMap(
        map: MutableMap<String, Int>,
    ): String {
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
                ${
            if (loggingEnabled) {
                "consumed map: [ ${logPlaceLabelMap(consumedMap)} ]"
            } else {
                "logging disabled"
            }
        }
                ${
            if (loggingEnabled) {
                "produced map: [ ${logPlaceLabelMap(producedMap)} ]"
            } else ""
        }
            )
        """.trimIndent()
    }

    companion object {
        const val UNSET_BINDING_INDEX = -1
        fun createEnabledBinding(
            transition: Transition,
        ): ActiveBinding? {
            val bindingEnabled = transition.isBindingEnabled()
            if (!bindingEnabled) return null
            return ActiveBinding(transition)
        }
    }
}
