package simulation

import model.ObjectMarking
import simulation.random.BindingSelector
import simulation.random.RandomFactory
import simulation.random.TokenSelector

data class SimulationParams(
    val templateOcNet: SimulatableComposedOcNet<*>,
    val initialMarking: ObjectMarking,
    val timeoutSec: Long?,
    val randomSeed: Long?,
)

class SimulationCreator(
    private val simulationParams: SimulationParams,
    private val executionConditions: ExecutionConditions,
    private val logger: Logger,
) {
    fun createSimulationTask(): SimulationTask {
        val copy = simulationParams.templateOcNet.fullCopy()

        val random = RandomFactory().create(simulationParams.randomSeed)

        return SimulationTask(
            simulationParams.copy(templateOcNet = copy),
            executionConditions,
            logger,
            bindingSelector = BindingSelector(random),
            tokenSelector = TokenSelector(random)
        )
    }
}
