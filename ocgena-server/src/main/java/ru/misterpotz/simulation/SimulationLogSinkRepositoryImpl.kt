package ru.misterpotz.simulation

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import ru.misterpotz.InAndOutPlacesColumnProducer
import ru.misterpotz.ObjectTokenMeta
import ru.misterpotz.SimulationStepLog
import ru.misterpotz.TokenSerializer
import ru.misterpotz.db.DBConnectionSetupper
import ru.misterpotz.ocgena.ocnet.OCNetStruct

internal class SimulationLogSinkRepositoryImpl(
    private val dbConnection: DBConnectionSetupper.Connection,
    private val tablesProvider: TablesProvider,
    private val inAndOutPlacesColumnProducer: InAndOutPlacesColumnProducer,
    private val ocNetStruct: OCNetStruct,
    private val tokenSerializer: TokenSerializer
) : SimulationLogRepository {
    private var checkedIfCreated = false
    private val db: Database = dbConnection.database

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
                        placesTable
                    )
                    with(tablesProvider) {
                        ocNetStruct.objectTypeRegistry.types.forEach { objectType ->
                            objectTypeTable.insert {
                                it[objectTypeId] = objectType.id
                                it[objectTypeLabel] =
                                    ocNetStruct.objectTypeRegistry[objectType.id].label
                            }
                        }
                        ocNetStruct.transitionsRegistry.forEach { transition ->
                            transitionToLabel.insert {
                                it[transitionId] = transition.id
                                it[transitionLabel] = transition.label
                            }
                        }
                        ocNetStruct.placeRegistry.places.forEach { place ->
                            placesTable.insert {
                                it[placeId] = place.id
                            }
                        }
                    }
                }
                checkedIfCreated = true
            }
            commit()
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
            commit()
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

    override suspend fun close() {
        dbConnection.close()
    }

    private suspend fun insertSteps(batch: List<SimulationStepLog>) {
        with(tablesProvider) {
            simulationStepsTable.batchInsert(batch) { simulationStepLog ->
                this[simulationStepsTable.id] = simulationStepLog.stepNumber
                this[SimulationStepsTable.clockIncrement] = simulationStepLog.clockIncrement
                this[SimulationStepsTable.totalClock] = simulationStepLog.totalClock
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

