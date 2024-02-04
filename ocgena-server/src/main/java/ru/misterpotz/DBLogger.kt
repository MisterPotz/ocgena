package ru.misterpotz

import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import ru.misterpotz.di.ServerSimulationConfig
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
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

interface TablesProvider {
    val simulationStepsTable: SimulationStepsTable
    val stepToMarkingAmountsTable: StepToMarkingAmountsTable
    val stepToFiringAmountsTable: StepToFiringAmountsTable
    val stepToFiringTokensTable: StepToFiringTokensTable
    val tokensTable: TokensTable
    val objectTypeTable: ObjectTypeTable
}

class TokenSerializer @Inject constructor() {
    fun serializeTokens(tokens: List<Long>): String {
        return tokens.joinToString(separator = ",") { it.toString() }
    }

    fun deserializeTokens(string: String): List<Long> {
        return string.split(",").map { it.toLong() }
    }
}

class InAndOutPlacesColumnProducer @Inject constructor(private val serverSimulationConfig: ServerSimulationConfig) {
    val IN_PREFIX = "in_"
    val OUT_PREFIX = "out_"

    val inPlaces = serverSimulationConfig.simulationConfig.ocNet.placeRegistry.places.map {
        "$IN_PREFIX${it.id}"
    }
    val outPlaces = serverSimulationConfig.simulationConfig.ocNet.placeRegistry.places.map {
        "$OUT_PREFIX${it.id}"
    }

    val merged = inPlaces.toMutableList().apply {
        addAll(outPlaces)
    }

    fun inNameTransformer(inPlace: String): String {
        return "$IN_PREFIX$inPlace"
    }

    fun outNameTransformer(outPlace: String): String {
        return "$OUT_PREFIX$outPlace"
    }
}

internal class TablesProviderImpl @Inject constructor(
    serverSimulationConfig: ServerSimulationConfig,
    inAndOutPlacesColumnProducer: InAndOutPlacesColumnProducer
) : TablesProvider {
    private val simulationConfig = serverSimulationConfig.simulationConfig
    private val placesTotal = simulationConfig.ocNet.placeRegistry.places.map { it.id }

    override val stepToMarkingAmountsTable = StepToMarkingAmountsTable(placesTotal)
    override val stepToFiringAmountsTable = StepToFiringAmountsTable(columnNames = inAndOutPlacesColumnProducer.merged)
    override val stepToFiringTokensTable = StepToFiringTokensTable(columnNames = inAndOutPlacesColumnProducer.merged)
    override val tokensTable = TokensTable
    override val objectTypeTable = ObjectTypeTable
    override val simulationStepsTable = SimulationStepsTable
}

interface SimulationLogRepository {
    suspend fun pushInitialData()
    suspend fun push(batch: List<SimulationStepLog>)
    suspend fun close()
}

class SimulationLogRepositoryImpl @Inject constructor(
    private val db: Database,
    private val hikariDataSource: HikariDataSource,
    private val tablesProvider: TablesProvider,
    private val simulationConfig: SimulationConfig,
    private val inAndOutPlacesColumnProducer: InAndOutPlacesColumnProducer,
    private val tokenSerializer: TokenSerializer
) : SimulationLogRepository {
    override suspend fun pushInitialData() {

        newSuspendedTransaction {
            addLogger(StdOutSqlLogger)
            with(tablesProvider) {
                SchemaUtils.create(
                    simulationStepsTable,
                    objectTypeTable,
                    tokensTable,
                    stepToMarkingAmountsTable,
                    stepToFiringAmountsTable,
                    stepToFiringTokensTable
                )
            }
        }

        newSuspendedTransaction {
            addLogger(StdOutSqlLogger)
            with(tablesProvider) {
                simulationConfig.ocNet.objectTypeRegistry.types.forEach { objectType ->
                    objectTypeTable.insert {
                        it[objectTypeId] = objectType.id
                    }
                }
            }
        }
    }

    override suspend fun push(batch: List<SimulationStepLog>) {
        newSuspendedTransaction(db = db) {
            addLogger(StdOutSqlLogger)

            insertSteps(batch)
            insertStepToMarkingAmounts(batch)
            insertStepToFiringAmounts(batch)
            insertSteptoFiringTokens(batch)
            insertTokens(batch)
        }
    }

    override suspend fun close() {
        hikariDataSource.close()
    }

    private fun insertSteps(batch: List<SimulationStepLog>) {
        with(tablesProvider) {
            simulationStepsTable.batchInsert(batch) { simulationStepLog ->
                this[simulationStepsTable.id] = simulationStepLog.stepNumber
                this[simulationStepsTable.clockIncrement] = simulationStepLog.clockIncrement
                this[simulationStepsTable.chosenTransition] = simulationStepLog.selectedFiredTransition
            }
        }
    }

    private fun insertStepToMarkingAmounts(batch: List<SimulationStepLog>) {
        with(tablesProvider) {
            stepToMarkingAmountsTable.batchInsert(batch) { simulationStepLog ->
                this[stepToMarkingAmountsTable.stepNumber] = simulationStepLog.stepNumber
                insertBatchMap(stepToMarkingAmountsTable, simulationStepLog.starterMarkingAmounts)
            }
        }
    }

    private fun insertStepToFiringAmounts(batch: List<SimulationStepLog>) {
        with(tablesProvider) {
            stepToFiringAmountsTable.batchInsert(batch) { simulationStepLog ->
                this[stepToFiringAmountsTable.stepNumber] = simulationStepLog.stepNumber
                insertBatchMap(
                    stepToFiringAmountsTable,
                    simulationStepLog.firingInMarkingAmounts,
                    inAndOutPlacesColumnProducer::inNameTransformer
                )
                insertBatchMap(
                    stepToFiringAmountsTable,
                    simulationStepLog.firingOutMarkingAmounts,
                    inAndOutPlacesColumnProducer::outNameTransformer
                )
            }
        }
    }

    private fun insertSteptoFiringTokens(batch: List<SimulationStepLog>) {
        with(tablesProvider) {

            stepToFiringTokensTable.batchInsert(batch) { simulationStepLog ->
                this[stepToFiringTokensTable.stepNumber] = simulationStepLog.stepNumber
                insertBatchMapTransform(
                    stepToFiringTokensTable,
                    simulationStepLog.firingInMarkingTokens,
                    inAndOutPlacesColumnProducer::inNameTransformer,
                    tokenSerializer::serializeTokens
                )
                insertBatchMapTransform(
                    stepToFiringTokensTable,
                    simulationStepLog.firingOutMarkingTokens,
                    inAndOutPlacesColumnProducer::outNameTransformer,
                    tokenSerializer::serializeTokens
                )
            }
        }
    }

    private fun insertTokens(batch: List<SimulationStepLog>) {
        with(tablesProvider) {
            val tokens = batch.flatMap { it.tokensInitializedAtStep }
            tokensTable.batchInsert(tokens) { token ->
                this[tokensTable.id] = token.id
                this[tokensTable.objectTypeId] = token.objectTypeId
            }
        }
    }
}