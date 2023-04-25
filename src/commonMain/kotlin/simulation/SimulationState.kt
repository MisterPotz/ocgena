package simulation

class SimulationState() {
    private var current: BySteps? = null

    fun onStart() {
        current = BySteps(noEnabledTransitions = false, noPlannedTransitions = false)
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

    fun isFinished(): Boolean {
        require(current != null)
        return current!!.noPlannedTransitions!! && current!!.noEnabledTransitions!!
    }

    private class BySteps(
        var noEnabledTransitions: Boolean? = null,
        var noPlannedTransitions: Boolean? = null,
    )
}
