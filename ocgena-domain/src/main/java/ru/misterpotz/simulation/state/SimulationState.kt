package ru.misterpotz.simulation.state

class SimulationState() {
    private var current: BySteps? = null
    var currentStep : Int = -1

    fun onStart() {
        current = BySteps(noEnabledTransitions = false, noPlannedTransitions = false, noPlannedTokenGenerations = false)
    }

    fun onNewStep() {
        current = BySteps()
    }

    fun onHasEnabledTransitions(hasEnabledTransitions: Boolean) {
        require(current != null && current?.noEnabledTransitions == null)
        current!!.noEnabledTransitions = !hasEnabledTransitions
    }

    fun onHasPlannedTransitions(hasPlannedTransitions: Boolean) {
        require(current != null && current?.noPlannedTransitions == null)
        current!!.noPlannedTransitions = !hasPlannedTransitions
    }

    fun onHasPlannedTokens(hasPlannedTokensToGenerate : Boolean) {
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
        var noPlannedTokenGenerations : Boolean? = null
    )
}
