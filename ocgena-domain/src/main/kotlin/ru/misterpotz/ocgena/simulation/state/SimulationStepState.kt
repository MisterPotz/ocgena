package ru.misterpotz.ocgena.simulation.state

class SimulationStepState {
    private var current: BySteps? = null
    var currentStep: Long = -1
    var stepIndex: Long = 0
    val oneStepGranularity = 5
    var stepInGranularityIterationsCounter: Long = 0


    fun onStart() {
        current = BySteps(noEnabledTransitions = false, noPlannedTransitions = false, noPlannedTokenGenerations = false)
    }

    fun onNewStep() {
        current = BySteps()
    }

    fun checkStepGranularityNotSatisfied(): Boolean {
        return stepInGranularityIterationsCounter < oneStepGranularity
    }

    fun resetStepGranularity() {
        stepInGranularityIterationsCounter = 0
    }

    fun incrementStep() {
        stepIndex++
        stepInGranularityIterationsCounter++
    }

    fun onHasEnabledTransitions(hasEnabledTransitions: Boolean) {
        require(current != null && current?.noEnabledTransitions == null)
        current!!.noEnabledTransitions = !hasEnabledTransitions
    }

    fun onHasPlannedTransitions(hasPlannedTransitions: Boolean) {
        require(current != null && current?.noPlannedTransitions == null)
        current!!.noPlannedTransitions = !hasPlannedTransitions
    }

    fun onHasPlannedTokens(hasPlannedTokensToGenerate: Boolean) {
        require(current != null && current?.noPlannedTokenGenerations == null)
        current!!.noPlannedTokenGenerations = !hasPlannedTokensToGenerate
    }

    fun isFinished(): Boolean {
        require(current != null)
        return current!!.noPlannedTransitions!!
                && current!!.noEnabledTransitions!!
                && current!!.noPlannedTokenGenerations!!
    }

    private class BySteps(
        var noEnabledTransitions: Boolean? = null,
        var noPlannedTransitions: Boolean? = null,
        var noPlannedTokenGenerations: Boolean? = null
    )
}
