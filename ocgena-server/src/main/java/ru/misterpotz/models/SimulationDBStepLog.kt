package ru.misterpotz.models

import ru.misterpotz.ObjectTokenMeta
import ru.misterpotz.SimulationLogTransition

data class SimulationDBStepLog(
    val stepNumber: Long,
    val totalClock: Long,
    val clockIncrement: Long,
    // dedicated event tables
    val selectedFiredTransition: SimulationLogTransition? = null,
    val tokenIdToObjectTypeId : Map<Long, String> = mutableMapOf(),
    val starterMarkingAmounts: Map<String, Int> = mutableMapOf(),
    val firingInMarkingAmounts: Map<String, Int> = mutableMapOf(),
    val firingOutMarkingAmounts: Map<String, Int> = mutableMapOf(),
    val firingInMarkingTokens: Map<String, List<Long>> = mutableMapOf(),
    val firingOutMarkingTokens: Map<String, List<Long>> = mutableMapOf(),
    // insert to respective object tables / object to object tables / and dedicated object table
    val transitionAllItems : List<Long> = emptyList(),
//    // event to object relation
//    val itemToOcelItem: List<Long> = emptyList()
)

data class SimulationLogsBatch(
    val logs: List<SimulationDBStepLog>,
    val totalTokens: List<ObjectTokenMeta>
)