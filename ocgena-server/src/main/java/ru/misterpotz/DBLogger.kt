package ru.misterpotz

import kotlinx.serialization.Serializable

data class SimulationStep(
    val stepNumber: Long,
    val clockIncrement: Int,
    val chosenTransitionId: String? = null
)

data class StepToMarkingAmounts(
    val stepNumber: Long,
    val placesToAmounts: Map<String, Int>
)

data class StepToFiringAmounts(
    val stepNumber: Long,
    val inPlacesToAmounts: Map<String, List<Int>>,
    val outPlacesToTokens: Map<String, List<Int>>
)

data class StepToFiringTokens(
    val stepNumber: Long,
    val inPlacesToTokens: Map<String, List<String>>,
    val outPlacesToTokens: Map<String, List<String>>
)

data class Token(val id: Long, val tokenObjectType: String)

data class Tokens(
    val tokens: List<Token>
)

class DBLoggerImpl(
    private val maxStoredBatchSize: Int = 10
) : DBLogger {
    private val batch: MutableList<SimulationStepLog> = mutableListOf()
    override suspend fun acceptStepLog(simulationStepLog: SimulationStepLog) {
        saveIfBatchMaxSize(forceSave = false)
    }

    override suspend fun simulationFinished() {
        saveIfBatchMaxSize(forceSave = true)
    }

    private fun batchMaxSizeCondition() : Boolean {
        return batch.size >= maxStoredBatchSize
    }

    private suspend fun saveIfBatchMaxSize(forceSave : Boolean = false) {
        if (batchMaxSizeCondition() || forceSave) {

        }
    }
}

class LogRepository() {

}