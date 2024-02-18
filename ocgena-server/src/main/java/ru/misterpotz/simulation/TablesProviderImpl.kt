package ru.misterpotz.simulation

import ru.misterpotz.InAndOutPlacesColumnProducer
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import javax.inject.Inject

internal class TablesProviderImpl @Inject constructor(
    ocNetStruct: OCNetStruct,
    inAndOutPlacesColumnProducer: InAndOutPlacesColumnProducer
) : TablesProvider {
    private val placesTotal = ocNetStruct.placeRegistry.places.map { it.id }

    override val stepToMarkingAmountsTable = StepToMarkingAmountsTable(placesTotal)
    override val stepToFiringAmountsTable = StepToFiringAmountsTable(columnNames = inAndOutPlacesColumnProducer.merged)
    override val stepToFiringTokensTable = StepToFiringTokensTable(columnNames = inAndOutPlacesColumnProducer.merged)
    override val tokensTable = TokensTable
    override val objectTypeTable = ObjectTypeTable
    override val transitionToLabel: TransitionToLabelTable = TransitionToLabelTable
    override val simulationStepsTable = SimulationStepsTable
}