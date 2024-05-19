package ru.misterpotz.simulation

import ru.misterpotz.ObjectTokenMeta
import ru.misterpotz.SimulationStepLog
import ru.misterpotz.models.SimulationDBStepLog

interface SimulationLogRepository {
    suspend fun push(batch: List<SimulationStepLog>)
    suspend fun getAllTokens() : List<ObjectTokenMeta>
    suspend fun close()
}

interface SimulationLogReadRepository{
    suspend fun readBatch(steps : LongRange) : List<SimulationDBStepLog>
    suspend fun totalSteps() : Long
}