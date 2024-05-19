package ru.misterpotz.convert

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import ru.misterpotz.InAndOutPlacesColumnProducer
import ru.misterpotz.SimulationLogTransition
import ru.misterpotz.TokenSerializer
import ru.misterpotz.db.DBConnectionSetupper
import ru.misterpotz.models.SimulationDBStepLog
import ru.misterpotz.simulation.*

class SimulationLogReadRepository(
    private val inAndOutPlacesColumnProducer: InAndOutPlacesColumnProducer,
    private val connection: DBConnectionSetupper.Connection,
    private val tablesProvider: TablesProvider,
    private val tokenSerializer: TokenSerializer
) {
    private val db = connection.database

    suspend fun totalSteps(): Long {
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

    suspend fun readBatch(steps: LongRange): List<SimulationDBStepLog> {
        return newSuspendedTransaction(db = db) {
            getSteps(steps.toList())
        }
    }

    private suspend fun getStep(resultRow: ResultRow): SimulationDBStepLog {
        return with(tablesProvider) {
            val stepToFiringAmountsMap = resultRow.getIntMap(stepToFiringAmountsTable)!!
            val stepToFiringTokens = resultRow.getStringMap(stepToFiringTokensTable)!!


            val chosenTransitionId = resultRow[SimulationStepsTable.chosenTransition]
            val transitionDuration = resultRow[SimulationStepsTable.transitionDuration]

            val selectedFiredTransition = if (chosenTransitionId != null && transitionDuration != null) {
                SimulationLogTransition(
                    chosenTransitionId,
                    transitionDuration
                )
            } else {
                null
            }

            val firingInMarkingAmounts = deTransformInPlaces(stepToFiringAmountsMap)
            val firingOutMarkingAmounts = deTransformOutPlaces(stepToFiringAmountsMap)
            val firingInMarkingTokens = detransformInPlacesString(stepToFiringTokens)
            val firingOutMarkingTokens = deTransformOutPlacesString(stepToFiringTokens)

            val totalTokens = firingInMarkingTokens.values.flatten().toMutableSet().apply {
                addAll(firingOutMarkingTokens.values.toMutableList().flatten())
            }
            val lookUpObjectIds =
                firingInMarkingTokens.values
                    .toMutableList()
                    .apply { addAll(firingOutMarkingTokens.values) }
                    .fold(mutableSetOf()) { set: MutableSet<Long>, tokens: List<Long> ->
                        set.addAll(tokens)
                        set
                    }

            val tokenIdToObjectTypeId = (tokensTable).select(tokensTable.columns)
                .where { TokensTable.tokenId inList lookUpObjectIds }
                .associate {
                    Pair(it[TokensTable.tokenId], it[TokensTable.objectTypeId])
                }

            SimulationDBStepLog(
                stepNumber = resultRow[simulationStepsTable.id].value,
                clockIncrement = resultRow[SimulationStepsTable.clockIncrement],
                selectedFiredTransition = selectedFiredTransition,
                starterMarkingAmounts = resultRow.getIntMap(stepToMarkingAmountsTable)!!,
                firingInMarkingAmounts = firingInMarkingAmounts,
                firingOutMarkingAmounts = firingOutMarkingAmounts,
                firingInMarkingTokens = firingInMarkingTokens,
                firingOutMarkingTokens = firingOutMarkingTokens,
                tokenIdToObjectTypeId = tokenIdToObjectTypeId,
                transitionAllItems = totalTokens.toList(),
                totalClock = resultRow[SimulationStepsTable.totalClock]
            )
        }
    }

    suspend fun close() {
        connection.close()
    }
    private suspend fun getSteps(stepIndeces: List<Long>): List<SimulationDBStepLog> {
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
}