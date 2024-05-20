package ru.misterpotz.convert

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import ru.misterpotz.InAndOutPlacesColumnProducer
import ru.misterpotz.SimulationLabellingData
import ru.misterpotz.TokenSerializer
import ru.misterpotz.db.DBConnectionSetupper
import ru.misterpotz.di.getOcelDB
import ru.misterpotz.di.getSimDB
import ru.misterpotz.models.SimulationDBStepLog
import ru.misterpotz.simulation.TablesProvider
import kotlin.sequences.Sequence

internal class OCNetToOCELConverter(
    private val ocelTablesProvider: OcelTablesProvider = OcelTablesProvider,
    private val databases: Map<
            @JvmSuppressWildcards String,
            @JvmSuppressWildcards DBConnectionSetupper.Connection>

) {

    private suspend fun getGeneralSimulationData(): SimulationLabellingData {
        val simDB = databases.getSimDB()

        return newSuspendedTransaction(db = simDB.database) {
            with(
                TablesProvider(emptyList())
            ) {
                val transitionToLabelMap = transitionToLabel.selectAll().associate { row ->
                    Pair(row[transitionToLabel.transitionId], row[transitionToLabel.transitionLabel])
                }

                val objectTypeIdToLabel = objectTypeTable.selectAll().associate { row ->
                    Pair(row[objectTypeTable.objectTypeId], row[objectTypeTable.objectTypeLabel])
                }
                val allPlaces = placesTable.selectAll().map { row -> row[placesTable.placeId] }

                SimulationLabellingData(transitionToLabelMap, objectTypeIdToLabel, allPlaces)
            }
        }
    }

    private suspend fun createBatchIterator(simulationLogRepository: SimulationLogReadRepository): Iterator<LongRange> {
        val totalCounts = simulationLogRepository.totalSteps()
        return BatchIterator(stride = 100L, totalCounts)
    }

    private suspend fun initializeTables(labelConverter: LabelConverter, labellingData: SimulationLabellingData) {
        newSuspendedTransaction(db = databases.getOcelDB().database) {
            addLogger(StdOutSqlLogger)
            with(ocelTablesProvider) {
                val tables = buildList {
                    add(eventsTable)
                    add(eventObjectsTable)
                    add(objectsTable)
                    add(objectObjectsTable)
                    addAll(labellingData.transitionIdToLabel.keys.map {
                        ocelTablesProvider.concreteEventTable(labelConverter.ocelEventTableName(it))
                    })
                    addAll(labellingData.objectTypeIdToLabel.keys.map {
                        ocelTablesProvider.concreteObjectTable(labelConverter.ocelObjectTableName(it))
                    })
                    add(eventTypeMap)
                    add(objectTypeMap)
                }
                SchemaUtils.create(
                    *tables.toTypedArray()
                )
                objectTypeMap.batchInsert(labellingData.objectTypeIdToLabel.toList()) {pair: Pair<String, String> ->
                    this[objectTypeMap.ocelType] = pair.first
                    this[objectTypeMap.ocelTypeMap] = labelConverter.ocelObjectTableNameNoPrefix(pair.first)
                }
                eventTypeMap.batchInsert(labellingData.transitionIdToLabel.toList()) {pair: Pair<String, String> ->
                    this[eventTypeMap.ocelType] = pair.first
                    this[eventTypeMap.ocelTypeMap] = labelConverter.ocelEventTableNameNoPrefix(pair.first)
                }
            }
        }
    }

    suspend fun convert() {
        val simulationLabellingData = getGeneralSimulationData()

        val tablesProvider = TablesProvider(simulationLabellingData.places)
        val simulationLogReadRepository = SimulationLogReadRepository(
            inAndOutPlacesColumnProducer = InAndOutPlacesColumnProducer(simulationLabellingData.places),
            connection = databases.getSimDB(),
            tablesProvider = tablesProvider,
            tokenSerializer = TokenSerializer
        )
        val labelConverter = LabelConverter(simulationLabellingData)
        val insertExtension = InsertExtension(
            labelConverter = labelConverter,
            ocelDb = databases.getOcelDB().database,
            ocelTablesProvider = ocelTablesProvider
        )

        initializeTables(labelConverter, simulationLabellingData)

        val iterator = createBatchIterator(simulationLogReadRepository)
        while (iterator.hasNext()) {
            val range = iterator.next()
            val items = simulationLogReadRepository.readBatch(range)
            insertExtension.insertAllLogs(items)
        }
    }
}

internal class InsertExtension(
    val labelConverter: LabelConverter,
    val ocelDb: Database,
    val ocelTablesProvider: OcelTablesProvider,
) {
    fun insertLog(log: SimulationDBStepLog) {
        if (log.selectedFiredTransition == null) return

        with(ocelTablesProvider) {
            val eventOcelId = labelConverter.ocelEventId(log)
            val eventTableName = labelConverter.ocelEventTableName(log)

            // concrete event / duplicates not possible
            concreteEventTable(eventTableName).let { table ->
                table.insert {
                    it[table.ocelEventId] = eventOcelId
                    it[table.ocelTime] = log.totalClock.toString()
                }
            }
            // inserting general event table / duplicates not possible
            eventsTable.insert {
                it[eventsTable.ocelEventId] = eventOcelId
                it[eventsTable.ocelEventType] = labelConverter.ocelEventType(log)
            }
            // event to object association duplicates are not possible (each event has unique id)
            eventObjectsTable.batchInsert(data = log.transitionAllItems) { item ->
                this[eventObjectsTable.ocelEventId] = eventOcelId
                this[eventObjectsTable.ocelObjectId] = labelConverter.ocelObjectId(log, item)
            }
            // concrete objects table insertion
            for (i in log.transitionAllItems) {
                val objectTableName = labelConverter.ocelObjectTableName(log, i)
                val objectOcelId = labelConverter.ocelObjectId(log, i)
                concreteObjectTable(objectTableName).let { table ->
                    table.insert {
                        it[table.ocelObjectId] = objectOcelId
                        it[table.ocelTime] = log.totalClock.toString()
                    }
                }
            }

            // object to object table, duplicates are prevented if qualifier same
            // make all associations
            val pairIterator = makeBubblePairCombinator(log.transitionAllItems)
            objectObjectsTable.batchInsert(data = pairIterator, ignore = true) { pair ->
                this[objectObjectsTable.ocelSourceObjectId] = labelConverter.ocelObjectId(log, pair.first)
                this[objectObjectsTable.ocelTargetObjectId] = labelConverter.ocelObjectId(log, pair.second)
            }

            // general object table, duplicates are prevented if time same
            objectsTable.batchInsert(data = log.transitionAllItems, ignore = true) { item ->
                val id = labelConverter.ocelObjectId(log, item)

                this[objectsTable.ocelObjectId] = id
                this[objectsTable.ocelObjectType] = labelConverter.ocelObjectType(log, item)
            }
        }
    }

    private fun makeBubblePairCombinator(tokens: List<Long>): Sequence<Pair<Long, Long>> {
        return iterator {
            val size = tokens.size
            if (size < 2) return@iterator

            for (i in 0..<tokens.lastIndex) {
                for (g in ((i + 1)..tokens.lastIndex)) {
                    yield(Pair(tokens[i], tokens[g]))
                }
            }
        }.asSequence()
    }

    suspend fun insertAllLogs(items: List<SimulationDBStepLog>) {
        newSuspendedTransaction(db = ocelDb) {
            items.forEach {
                insertLog(it)
            }
        }
    }
}


class LabelConverter(private val simulationLabellingData: SimulationLabellingData) {
    fun ocelEventId(
        simulationDBStepLog: SimulationDBStepLog,
    ): String {
        val transitionId = simulationDBStepLog.selectedFiredTransition!!.transitionId
        val transitionLabel = simulationLabellingData.transitionIdToLabel[transitionId]!!
        val step = simulationDBStepLog.stepNumber

        return "${transitionLabel.toId()}_$step"
    }

    fun ocelEventType(simulationDBStepLog: SimulationDBStepLog): String {
        val transitionId = simulationDBStepLog.selectedFiredTransition!!.transitionId
        return simulationLabellingData.transitionIdToLabel[transitionId]!!
    }

    fun ocelObjectType(simulationDBStepLog: SimulationDBStepLog, tokenId: Long): String {
        val objectTypeId = simulationDBStepLog.tokenIdToObjectTypeId[tokenId]!!
        return simulationLabellingData.objectTypeIdToLabel[objectTypeId]!!
    }

    fun ocelEventTableName(simulationDBStepLog: SimulationDBStepLog): String {
        val oceleventType = ocelEventType(simulationDBStepLog)
        return "event_${oceleventType.toMapType()}"
    }

    fun ocelEventTableName(transitionId: String): String {
        val ocelEventType = simulationLabellingData.transitionIdToLabel[transitionId]!!
        return "event_${ocelEventType.toMapType()}"
    }
    fun ocelEventTableNameNoPrefix(transitionId: String): String {
        val ocelEventType = simulationLabellingData.transitionIdToLabel[transitionId]!!
        return ocelEventType.toMapType()
    }

    fun ocelObjectTableName(objectTypeId: String): String {
        val ocelEventType = simulationLabellingData.objectTypeIdToLabel[objectTypeId]!!
        return "object_${ocelEventType.toMapType()}"
    }

    fun ocelObjectTableNameNoPrefix(objectTypeId: String): String {
        val ocelEventType = simulationLabellingData.objectTypeIdToLabel[objectTypeId]!!
        return ocelEventType.toMapType()
    }

    fun ocelObjectId(simulationDBStepLog: SimulationDBStepLog, tokenId: Long): String {
        val objectTypeId = simulationDBStepLog.tokenIdToObjectTypeId[tokenId]!!
        val objectTypeIdLabel = simulationLabellingData.objectTypeIdToLabel[objectTypeId]!!
        return "${objectTypeIdLabel.toId()}_$tokenId"
    }

    fun ocelObjectTableName(simulationDBStepLog: SimulationDBStepLog, tokenId: Long): String {
        val objectTypeLabel = ocelObjectType(simulationDBStepLog, tokenId)
        return "object_${objectTypeLabel.toMapType()}"
    }

    private fun String.toId(): String {
        return this.split(" ")
            .takeIf { it.size >= 2 }
            ?.joinToString(separator = "") { it.first().lowercase() }
            ?: this
    }

    private fun String.toMapType(): String {
        return split(" ").joinToString(separator = "") { it.replaceFirstChar { it.uppercase() } }
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
