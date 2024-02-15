package ru.misterpotz.db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.firstValue
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import ru.misterpotz.*
import ru.misterpotz.models.SimulationDBStepLog
import ru.misterpotz.ocgena.simulation.config.SimulationConfig

class SimulationLogRepositoryImpl(
    private val db: Database,
    private val tablesProvider: TablesProvider,
    private val simulationConfig: SimulationConfig,
    private val inAndOutPlacesColumnProducer: InAndOutPlacesColumnProducer,
    private val tokenSerializer: TokenSerializer
) : SimulationLogRepository {
    private var checkedIfCreated = false
    private suspend fun initializeTables() {
        if (checkedIfCreated) return
        newSuspendedTransaction(db = db) {
            addLogger(StdOutSqlLogger)
            with(tablesProvider) {
                if (!objectTypeTable.exists()) {
                    SchemaUtils.create(
                        simulationStepsTable,
                        objectTypeTable,
                        tokensTable,
                        stepToMarkingAmountsTable,
                        stepToFiringAmountsTable,
                        stepToFiringTokensTable
                    )
                    with(tablesProvider) {
                        simulationConfig.ocNet.objectTypeRegistry.types.forEach { objectType ->
                            objectTypeTable.insert {
                                it[objectTypeId] = objectType.id
                            }
                        }
                    }
                }
                checkedIfCreated = true
            }
        }
    }

    override suspend fun push(batch: List<SimulationStepLog>) {
        initializeTables()
        newSuspendedTransaction(db = db) {
            addLogger(StdOutSqlLogger)

            insertSteps(batch)
            insertStepToMarkingAmounts(batch)
            insertStepToFiringAmounts(batch)
            insertSteptoFiringTokens(batch)
            insertTokens(batch)
        }
    }

    private fun getStep(resultRow: ResultRow): SimulationDBStepLog {
        return with(tablesProvider) {
            val stepToFiringAmountsMap = resultRow.getIntMap(stepToFiringAmountsTable)!!
            val stepToFiringTokens = resultRow.getStringMap(stepToFiringTokensTable)!!

            SimulationDBStepLog(
                stepNumber = resultRow[simulationStepsTable.id].value,
                clockIncrement = resultRow[SimulationStepsTable.clockIncrement],
                selectedFiredTransition = resultRow[SimulationStepsTable.chosenTransition],
                firedTransitionDuration = resultRow[SimulationStepsTable.transitionDuration],
                starterMarkingAmounts = resultRow.getIntMap(stepToMarkingAmountsTable)!!,
                firingInMarkingAmounts = deTransformInPlaces(stepToFiringAmountsMap),
                firingOutMarkingAmounts = deTransformInPlaces(stepToFiringAmountsMap),
                firingInMarkingTokens = detransformInPlacesString(stepToFiringTokens),
                firingOutMarkingTokens = deTransformOutPlacesString(stepToFiringTokens),
            )
        }
    }

    private fun getSteps(stepIndeces: List<Long>): List<SimulationDBStepLog> {
        return with(tablesProvider) {

            (simulationStepsTable innerJoin
                    stepToMarkingAmountsTable innerJoin
                    stepToFiringAmountsTable innerJoin
                    stepToFiringTokensTable)
                .select(
                    listOf(
                        simulationStepsTable.columns,
                        stepToMarkingAmountsTable.columns,
                        stepToFiringAmountsTable.columns,
                        stepToFiringTokensTable.columns,
                        objectTypeTable.columns
                    ).flatten()
                ).where {
                    simulationStepsTable.id.inList(stepIndeces)
                }.map {
                    getStep(it)
                }
        }
    }

    private fun deTransformInPlaces(
        inMap: Map<String, Int>,
    ): Map<String, Int> {
        return inMap.mapKeys { inAndOutPlacesColumnProducer.inNameDetransformer(it.key) }
    }

    private fun deTransformOutPlaces(
        inMap: Map<String, Int>,
    ): Map<String, Int> {
        return inMap.mapKeys { inAndOutPlacesColumnProducer.outNameDetransformer(it.key) }
    }

    private fun detransformInPlacesString(
        inMap: Map<String, String?>,
    ): Map<String, List<Long>> {
        return inMap.mapKeys { inAndOutPlacesColumnProducer.inNameDetransformer(it.key) }
            .mapValues {
                tokenSerializer.deserializeTokens(it.value ?: return@mapValues listOf())
            }
    }

    private fun deTransformOutPlacesString(
        inMap: Map<String, String?>,
    ): Map<String, List<Long>> {
        return inMap.mapKeys { inAndOutPlacesColumnProducer.outNameDetransformer(it.key) }
            .mapValues {
                tokenSerializer.deserializeTokens(it.value ?: return@mapValues listOf())
            }
    }


    override suspend fun readBatch(steps: LongRange): List<SimulationDBStepLog> {
        return newSuspendedTransaction(db = db) {
            getSteps(steps.toList())
        }
    }

    override suspend fun totalSteps(): Long {
        return newSuspendedTransaction(db = db) {
            with(tablesProvider) {
                (simulationStepsTable).select(simulationStepsTable.id.count())
                    .first()
                    .let {
                        it[simulationStepsTable.id.count()]
                    }
            }
        }
    }


    override suspend fun getAllTokens(): List<ObjectTokenMeta> {
        return newSuspendedTransaction {
            with(tablesProvider) {
                tokensTable.select(tokensTable.columns)
                    .map {
                        ObjectTokenMeta(
                            id = it[tokensTable.id].value,
                            objectTypeId = it[TokensTable.objectTypeId]
                        )
                    }
            }
        }
    }

    private fun insertSteps(batch: List<SimulationStepLog>) {
        with(tablesProvider) {
            simulationStepsTable.batchInsert(batch) { simulationStepLog ->
                this[simulationStepsTable.id] = simulationStepLog.stepNumber
                this[SimulationStepsTable.clockIncrement] = simulationStepLog.clockIncrement
                this[SimulationStepsTable.chosenTransition] = simulationStepLog.selectedFiredTransition
                this[SimulationStepsTable.transitionDuration] = simulationStepLog.firedTransitionDuration
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
                this[TokensTable.objectTypeId] = token.objectTypeId
            }
        }
    }
}