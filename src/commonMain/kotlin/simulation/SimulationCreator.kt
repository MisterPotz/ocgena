package simulation

class SimulationCreator(
    private val templateOcNet: SimulatableComposedOcNet<*>,
    private val executionConditions: ExecutionConditions,
    private val logger: Logger,
) {
    fun createSimulationTask(): SimulationTask {
        val copy = templateOcNet.fullCopy()

        return SimulationTask(
            copy,
            executionConditions,
            logger,
            randomBindingSelector = RandomBindingSelector(),
        )
    }
}
