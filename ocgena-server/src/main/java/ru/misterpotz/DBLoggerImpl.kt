package ru.misterpotz

import javax.inject.Inject

class DBLoggerImpl @Inject constructor(
    private val simulationLogRepository: SimulationLogRepository,
) : DBLogger {
    private val maxStoredBatchSize: Int = 10
    private val batch: MutableList<SimulationStepLog> = mutableListOf()
    override suspend fun simulationPrepared() {
        simulationLogRepository.pushInitialData()
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