package ru.misterpotz

import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import javax.inject.Inject

class SimulationLogRepositoryImpl @Inject constructor(
    private val db: Database,
    private val hikariDataSource: HikariDataSource,
    private val tablesProvider: TablesProvider,
    private val simulationConfig: SimulationConfig,
    private val inAndOutPlacesColumnProducer: InAndOutPlacesColumnProducer,
    private val tokenSerializer: TokenSerializer
) : SimulationLogRepository {
    private var checkedIfCreated = false
    private suspend fun initializeTables() {
        if (checkedIfCreated) return
        newSuspendedTransaction {
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

    private suspend fun getStep(stepIndex: Int): SimulationStepLog {
        with(tablesProvider) {

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
                    this[simulationStepsTable.id] == stepIndex
                }

            this[simulationStepsTable.id]
        }
    }

    override suspend fun readBatch(steps: IntRange): List<SimulationStepLog> {
        val simulanewSuspendedTransaction = {

        }
    }

    override suspend fun close() {
        hikariDataSource.close()
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