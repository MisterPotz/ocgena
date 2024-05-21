package ru.misterpotz

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        withContext(Dispatchers.IO) {
            saveIfBatchMaxSize(forceSave = false)

        }
    }

    override suspend fun simulationFinished() {
        withContext(Dispatchers.IO) {
            saveIfBatchMaxSize(forceSave = true)
            simulationLogRepository.close()
        }
    }

    private fun batchMaxSizeCondition(): Boolean {
        return batch.size >= maxStoredBatchSize
    }

    private suspend fun saveIfBatchMaxSize(forceSave: Boolean = false) {
        if (batchMaxSizeCondition() || forceSave) {
            println("saving batch")
            simulationLogRepository.push(batch)
            batch.clear()
        }
    }
}