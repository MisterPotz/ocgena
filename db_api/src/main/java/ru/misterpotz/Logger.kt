package ru.misterpotz

interface Logger {
    suspend fun simulationPrepared()
    suspend fun acceptStepLog(simulationStepLog: SimulationStepLog)
    suspend fun simulationFinished()
}

data class ObjectTokenMeta(
    val id: Long,
    val objectTypeId: String
)

data class ObjectTypes(val listObjectTypes: List<String>)

data class SimulationLogTransition(
    val transitionId: String,
    val transitionDuration: Long
) {
    override fun toString(): String {
        return "($transitionId', duration=$transitionDuration)"
    }
}

data class SimulationStepLog(
    val stepNumber: Long,
    val clockIncrement: Long,
    val totalClock: Long,
    val selectedFiredTransition: SimulationLogTransition? = null,
    val starterMarkingAmounts: Map<String, Int> = mutableMapOf(),
    val endStepMarkingAmounts: Map<String, Int>? = null,
    val firingInMarkingAmounts: Map<String, Int> = mutableMapOf(),
    val firingOutMarkingAmounts: Map<String, Int> = mutableMapOf(),
    val firingInMarkingTokens: Map<String, List<Long>> = mutableMapOf(),
    val firingOutMarkingTokens: Map<String, List<Long>> = mutableMapOf(),
//    var timePNTransitionMarking : Map<String, Long>? = null,
    val tokensInitializedAtStep: List<ObjectTokenMeta>,
) {
    fun Map<String, Int>.prettyString(): String {
        return map { (p, i) -> "$p → $i" }.joinToString(", ", "{", "}")
    }

    fun Map<String, List<Long>>.prettyStringLongs(): String {
        return buildString {
            forEach { t, u ->
                appendLine("$t → [${u.joinToString(", ")}]")
            }
        }
    }

    fun prettyString(): String {
        return buildString {
            appendLine("steplog $stepNumber:")
            buildString {
                appendLine("clock increment: $clockIncrement")
                appendLine("transition: $selectedFiredTransition")
                appendLine("marking at start:")
                appendLine(starterMarkingAmounts.prettyString().prependIndent())

                appendLine("firing in amounts:")
                firingInMarkingAmounts.prettyString().prependIndent().let(::appendLine)
                appendLine("firing out amounts:")
                firingOutMarkingAmounts.prettyString().prependIndent().let(::appendLine)
                appendLine("firing in tokens:")
                firingInMarkingTokens.prettyStringLongs().prependIndent().let(::appendLine)
                appendLine("firing out tokens:")
                firingOutMarkingTokens.prettyStringLongs().prependIndent().let(::appendLine)
                appendLine("marking at end:")
                appendLine(endStepMarkingAmounts?.prettyString()?.prependIndent())
            }.prependIndent().let { appendLine(it) }
        }
    }

    override fun toString(): String {
        return "SimulationStepLog(stepNumber=$stepNumber, clockIncrement=$clockIncrement, selectedFiredTransition=$selectedFiredTransition, starterMarkingAmounts=$starterMarkingAmounts, endStepMarkingAmounts=$endStepMarkingAmounts, firingInMarkingAmounts=$firingInMarkingAmounts, firingOutMarkingAmounts=$firingOutMarkingAmounts, firingInMarkingTokens=$firingInMarkingTokens, firingOutMarkingTokens=$firingOutMarkingTokens, tokensInitializedAtStep=$tokensInitializedAtStep)"
    }
}

data class SimulationLabellingData(
    val transitionIdToLabel: Map<String, String>,
    val objectTypeIdToLabel: Map<String, String>,
    val places: List<String>
)