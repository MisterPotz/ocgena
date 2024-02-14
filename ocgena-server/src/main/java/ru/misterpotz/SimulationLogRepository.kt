package ru.misterpotz

interface SimulationLogRepository {
    suspend fun push(batch: List<SimulationStepLog>)
    suspend fun readBatch(steps : IntRange) : List<SimulationStepLog>
    suspend fun close()
}