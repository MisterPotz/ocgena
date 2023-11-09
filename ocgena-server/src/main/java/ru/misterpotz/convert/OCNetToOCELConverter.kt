package ru.misterpotz.convert

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import ru.misterpotz.SimulationGeneralData
import ru.misterpotz.db.DBConnectionSetupper
import ru.misterpotz.di.getOcelDB
import ru.misterpotz.di.getSimDB
import ru.misterpotz.simulation.SimulationLogReadRepository
import ru.misterpotz.simulation.SimulationLogRepository
import ru.misterpotz.simulation.TablesProvider
import java.lang.IllegalStateException
import javax.inject.Inject

internal class OCNetToOCELConverter @Inject constructor(
    private val simulationLogRepository: SimulationLogReadRepository,
    private val tableProvider: TablesProvider,
    private val insertExtensionAssistedFactory: InsertExtensionAssistedFactory,
    private val labelConverterAssistedFactory: LabelConverterAssistedFactory,
    private val databases: Map<
            @JvmSuppressWildcards String,
            @JvmSuppressWildcards DBConnectionSetupper.Connection>

) {

    private suspend fun getGeneralSimulationData(): SimulationGeneralData {
        val simDB = databases.getSimDB()

        return newSuspendedTransaction(db = simDB.database) {
            with(tableProvider) {
                val transitionToLabelMap = transitionToLabel.selectAll().associate { row ->
                    Pair(row[transitionToLabel.transitionId], row[transitionToLabel.transitionLabel])
                }

                val objectTypeIdToLabel = objectTypeTable.selectAll().associate { row ->
                    Pair(row[objectTypeTable.objectTypeId], row[objectTypeTable.objectTypeLabel])
                }

                SimulationGeneralData(transitionToLabelMap, objectTypeIdToLabel)
            }
        }
    }


    private suspend fun createBatchIterator(): Iterator<LongRange> {
        val totalCounts = simulationLogRepository.totalSteps()
        return BatchIterator(stride = 100L, totalCounts)
    }

    suspend fun convert() {
        val generalSimulationData = getGeneralSimulationData()

        val insertExtension = insertExtensionAssistedFactory.create(
            labelConverter = labelConverterAssistedFactory.create(generalSimulationData),
            db = databases.getOcelDB().database,
            simulationGeneralData = generalSimulationData
        )

        flow {
            createBatchIterator().forEach {
                emit(simulationLogRepository.readBatch(it))
            }
        }.transform { it ->
            it.forEach { emit(it) }
        }.collect {
            insertExtension.insertLog(it)
        }
    }
}

class BatchIterator(
    private val stride: Long,
    private val count: Long
) : Iterator<LongRange> {
    private var lastRange: LongRange? = null
    override fun hasNext(): Boolean {
        return lastRange?.let { lastRange!!.last < count - 1 } ?: (count > 0)
    }

    override fun next(): LongRange {
        val begin = lastRange?.last?.let { it + 1L } ?: 0L
        val end = (begin + stride - 1).coerceAtMost(count - 1)

        val newRange = begin..end
        if (newRange.isEmpty()) throw IllegalStateException()
        lastRange = newRange
        return newRange
    }
}
