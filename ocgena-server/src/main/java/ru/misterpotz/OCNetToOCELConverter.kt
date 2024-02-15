package ru.misterpotz

import ru.misterpotz.db.DBConnectionSetupper
import ru.misterpotz.db.SimulationLogRepository
import ru.misterpotz.di.getOcelDB
import ru.misterpotz.di.getSimDB
import ru.misterpotz.models.SimulationDBStepLog
import javax.inject.Inject

class OCNetToOCELConverter @Inject constructor(
    private val simulationLogRepository: SimulationLogRepository,
    private val databases: Map<
            @JvmSuppressWildcards String,
            @JvmSuppressWildcards DBConnectionSetupper.Connection>

) {
    suspend fun convert() {
        val simDB = databases.getSimDB()
        val ocelDB = databases.getOcelDB()

        val stride = 100L

        val totalCounts = simulationLogRepository.totalSteps()
        var i = 0
        while (i <= totalCounts) {
            val rangeEnd = (i + stride).coerceAtMost(totalCounts)
            val range = i until rangeEnd
            val batch = simulationLogRepository.readBatch(range)


        }
    }

    data class OCELDBRecord(

    )
    private fun convertLog(simulationDBStepLog: SimulationDBStepLog) {

    }
}
