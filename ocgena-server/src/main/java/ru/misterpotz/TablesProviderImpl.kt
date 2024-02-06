package ru.misterpotz

import ru.misterpotz.di.ServerSimulationConfig
import javax.inject.Inject

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