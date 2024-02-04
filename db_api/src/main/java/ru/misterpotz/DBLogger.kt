package ru.misterpotz

interface DBLogger {
    suspend fun acceptStepLog(simulationStepLog: SimulationStepLog)
    suspend fun simulationFinished()
}

data class ObjectTokenMeta(
    val id: Long,
    val objectTypeId: String
)


data class SimulationStepLog(
    val stepNumber: Long,
    val clockIncrement: Long,
    val selectedFiredTransition: String? = null,
    val starterMarkingAmounts: Map<String, Int> = mutableMapOf(),
    val firingInMarkingAmounts: Map<String, Int> = mutableMapOf(),
    val firingOutMarkingAmounts: Map<String, Int> = mutableMapOf(),
    val firingInMarkingTokens: Map<String, List<Long>> = mutableMapOf(),
    val firingOutMarkingTokens: Map<String, List<Long>> = mutableMapOf(),
    val tokensInitializedAtStep: List<ObjectTokenMeta>
)
