package ru.misterpotz.models

import ru.misterpotz.ObjectTokenMeta

data class SimulationDBStepLog(
    val stepNumber: Long,
    val clockIncrement: Long,
    val selectedFiredTransition: String? = null,
    val firedTransitionDuration: Long,
    val starterMarkingAmounts: Map<String, Int> = mutableMapOf(),
    val firingInMarkingAmounts: Map<String, Int> = mutableMapOf(),
    val firingOutMarkingAmounts: Map<String, Int> = mutableMapOf(),
    val firingInMarkingTokens: Map<String, List<Long>> = mutableMapOf(),
    val firingOutMarkingTokens: Map<String, List<Long>> = mutableMapOf(),
)

data class SimulationLogsBatch(
    val logs: List<SimulationDBStepLog>,
    val totalTokens: List<ObjectTokenMeta>
)