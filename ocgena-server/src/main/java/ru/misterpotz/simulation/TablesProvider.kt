package ru.misterpotz.simulation

interface TablesProvider {
    val simulationStepsTable: SimulationStepsTable
    val stepToMarkingAmountsTable: StepToMarkingAmountsTable
    val stepToFiringAmountsTable: StepToFiringAmountsTable
    val stepToFiringTokensTable: StepToFiringTokensTable
    val tokensTable: TokensTable
    val objectTypeTable: ObjectTypeTable
    val transitionToLabel: TransitionToLabelTable
}