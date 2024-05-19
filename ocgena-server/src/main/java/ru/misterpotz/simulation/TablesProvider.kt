package ru.misterpotz.simulation

import ru.misterpotz.InAndOutPlacesColumnProducer
import ru.misterpotz.ocgena.ocnet.OCNetStruct

class TablesProvider(
    ocNetStruct: OCNetStruct,
    inAndOutPlacesColumnProducer: InAndOutPlacesColumnProducer
) {
    private val placesTotal = ocNetStruct.placeRegistry.places.map { it.id }

    val stepToMarkingAmountsTable = StepToMarkingAmountsTable(placesTotal)
    val stepToFiringAmountsTable = StepToFiringAmountsTable(columnNames = inAndOutPlacesColumnProducer.merged)
    val stepToFiringTokensTable = StepToFiringTokensTable(columnNames = inAndOutPlacesColumnProducer.merged)
    val tokensTable = TokensTable
    val objectTypeTable = ObjectTypeTable
    val transitionToLabel: TransitionToLabelTable = TransitionToLabelTable
    val simulationStepsTable = SimulationStepsTable
}