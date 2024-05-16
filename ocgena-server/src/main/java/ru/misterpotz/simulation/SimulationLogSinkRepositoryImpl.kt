package ru.misterpotz.simulation

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import ru.misterpotz.InAndOutPlacesColumnProducer
import ru.misterpotz.ObjectTokenMeta
import ru.misterpotz.SimulationStepLog
import ru.misterpotz.TokenSerializer
import ru.misterpotz.ocgena.simulation_old.config.SimulationConfig

class SimulationLogSinkRepositoryImpl(
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
                        stepToFiringTokensTable,
                        transitionToLabel,
                    )
                    with(tablesProvider) {
                        simulationConfig.ocNet.objectTypeRegistry.types.forEach { objectType ->
                            objectTypeTable.insert {
                                it[objectTypeId] = objectType.id
                                it[objectTypeLabel] =
                                    simulationConfig.nodeToLabelRegistry.getObjectTypeLabel(objectType.id)
                            }
                        }
                        simulationConfig.nodeToLabelRegistry.transitionsToActivity.forEach { (t, u) ->
                            transitionToLabel.insert {
                                it[transitionId] = t
                                it[transitionLabel] = u
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


    override suspend fun getAllTokens(): List<ObjectTokenMeta> {
        return newSuspendedTransaction {
            with(tablesProvider) {
                tokensTable.select(tokensTable.columns)
                    .map {
                        ObjectTokenMeta(
                            id = it[tokensTable.tokenId],
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
                val selectedFiredTranstion = simulationStepLog.selectedFiredTransition
                if (selectedFiredTranstion != null) {
                    this[SimulationStepsTable.chosenTransition] = selectedFiredTranstion.transitionId
                    this[SimulationStepsTable.transitionDuration] = selectedFiredTranstion.transitionDuration
                }
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
                this[tokensTable.tokenId] = token.id
                this[TokensTable.objectTypeId] = token.objectTypeId
            }
        }
    }
}

