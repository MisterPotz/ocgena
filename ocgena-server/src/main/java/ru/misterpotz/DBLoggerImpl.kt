package ru.misterpotz

import com.zaxxer.hikari.HikariDataSource
import ru.misterpotz.db.DBConnectionSetupper
import ru.misterpotz.db.SimulationLogRepository
import javax.inject.Inject


interface SimulationFinishedNotifier {
    fun simulationFinished()
}

class SimulationFinishedNotifierImpl @Inject constructor(
    private val openedConnections: @JvmSuppressWildcards Map<@JvmSuppressWildcards String, @JvmSuppressWildcards DBConnectionSetupper.Connection>
) : SimulationFinishedNotifier {
    override fun simulationFinished() {
        for (i in openedConnections) {
            i.value.hikariDataSource.close()
        }
    }
}

class DBLoggerImpl @Inject constructor(
    private val simulationLogRepository: SimulationLogRepository,
    private val simulationFinishedNotifier: SimulationFinishedNotifier
) : DBLogger {
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
        simulationFinishedNotifier.simulationFinished()
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