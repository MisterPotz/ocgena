package simulation

import model.ObjectMarking
import kotlin.time.Duration

data class SimulationParams(
    val templateOcNet: SimulatableComposedOcNet<*>,
    val initialMarking: ObjectMarking,
    val timeoutSec: Long
)

class SimulationCreator(
    private val simulationParams: SimulationParams,
    private val executionConditions: ExecutionConditions,
    private val logger: Logger,
) {
    fun createSimulationTask(): SimulationTask {
        val copy = simulationParams.templateOcNet.fullCopy()

        return SimulationTask(
            simulationParams.copy(templateOcNet = copy),
            executionConditions,
            logger,
            randomBindingSelector = RandomBindingSelector(),
        )
    }
}
