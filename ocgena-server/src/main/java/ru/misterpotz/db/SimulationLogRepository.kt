package ru.misterpotz.db

import ru.misterpotz.ObjectTokenMeta
import ru.misterpotz.SimulationStepLog
import ru.misterpotz.models.SimulationDBStepLog

interface SimulationLogRepository {
    suspend fun push(batch: List<SimulationStepLog>)
    suspend fun readBatch(steps : LongRange) : List<SimulationDBStepLog>
    suspend fun getAllTokens() : List<ObjectTokenMeta>
}