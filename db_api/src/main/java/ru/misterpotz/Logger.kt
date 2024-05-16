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
    val transitionId : String,
    val transitionDuration: Long
)

data class SimulationStepLog(
    val stepNumber: Long,
    val clockIncrement: Long,
    val selectedFiredTransition: SimulationLogTransition? = null,
    val starterMarkingAmounts: Map<String, Int> = mutableMapOf(),
    val endStepMarkingAmounts : Map<String, Int>? = null,
    val firingInMarkingAmounts: Map<String, Int> = mutableMapOf(),
    val firingOutMarkingAmounts: Map<String, Int> = mutableMapOf(),
    val firingInMarkingTokens: Map<String, List<Long>> = mutableMapOf(),
    val firingOutMarkingTokens: Map<String, List<Long>> = mutableMapOf(),
//    var timePNTransitionMarking : Map<String, Long>? = null,
    val tokensInitializedAtStep: List<ObjectTokenMeta>,
)

data class SimulationGeneralData(
    val transitionIdToLabel: Map<String, String>,
    val objectTypeIdToLabel : Map<String, String>,
)