package ru.misterpotz.simulation

import ru.misterpotz.InAndOutPlacesColumnProducer
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Place

class TablesProvider(
    places: List<String> = emptyList(),
) {
    private val inAndOutPlacesColumnProducer: InAndOutPlacesColumnProducer by lazy {
        InAndOutPlacesColumnProducer(places)
    }
    private val placesTotal = places

    val stepToMarkingAmountsTable = StepToMarkingAmountsTable(placesTotal)
    val stepToFiringAmountsTable by lazy {
        StepToFiringAmountsTable(columnNames = inAndOutPlacesColumnProducer.merged)
    }
    val stepToFiringTokensTable by lazy {
        StepToFiringTokensTable(columnNames = inAndOutPlacesColumnProducer.merged)
    }
    val tokensTable = TokensTable
    val objectTypeTable = ObjectTypeTable
    val transitionToLabel: TransitionToLabelTable = TransitionToLabelTable
    val simulationStepsTable = SimulationStepsTable
    val placesTable = PlacesTable
}