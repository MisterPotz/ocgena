package ru.misterpotz

interface SimulationLogRepository {
    suspend fun pushInitialData()
    suspend fun push(batch: List<SimulationStepLog>)
    suspend fun close()
}