package ru.misterpotz

import ru.misterpotz.simulation.SimulationLogRepository
import javax.inject.Inject



class DBLoggerImpl @Inject constructor(
    private val simulationLogRepository: SimulationLogRepository,
) : Logger {
    private val maxStoredBatchSize: Int = 10
    private val batch: MutableList<SimulationStepLog> = mutableListOf()
    override suspend fun simulationPrepared() {
    }

    override suspend fun acceptStepLog(simulationStepLog: SimulationStepLog) {
        batch.add(simulationStepLog)
        saveIfBatchMaxSize(forceSave = false)
    }

    override suspend fun simulationFinished() {
        saveIfBatchMaxSize(forceSave = true)
        simulationLogRepository.close()
    }

    private fun batchMaxSizeCondition(): Boolean {
        return batch.size >= maxStoredBatchSize
    }

    private suspend fun saveIfBatchMaxSize(forceSave: Boolean = false) {
        if (batchMaxSizeCondition() || forceSave) {
            simulationLogRepository.push(batch)
            batch.clear()
        }
    }
}