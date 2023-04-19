package simulation

import model.WellFormedOCNet

class SimulationCreator(
    private val templateOcNet: WellFormedOCNet,
    private val executionConditions: ExecutionConditions,
    private val logger: Logger,
    private val simulationParams: SimulationParams
) {
    fun createSimulationTask(): SimulationTask {
        val copy = templateOcNet.fullCopy()
        return SimulationTask(copy, executionConditions, logger, simulationParams = simulationParams)
    }
}
