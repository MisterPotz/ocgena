package simulation

import model.ObjectMarking
import simulation.random.BindingSelector
import simulation.random.RandomFactory
import simulation.random.TokenSelector
import simulation.time.TransitionDurationSelector
import simulation.time.TransitionInstanceOccurenceDeltaSelector

data class SimulationParams(
    val templateOcNet: SimulatableComposedOcNet<*>,
    val initialMarking: ObjectMarking,
    val timeoutSec: Long?,
    val randomSeed: Long?,
    val useRandom : Boolean = true,
)

class SimulationCreator(
    private val simulationParams: SimulationParams,
    private val executionConditions: ExecutionConditions,
    private val logger: LoggerFactory = LoggerFactoryDefault,
) {
    fun createSimulationTask(): SimulationTask {
        val copy = simulationParams.templateOcNet.fullCopy()

        val random = RandomFactory().create(simulationParams.randomSeed).takeIf {
            simulationParams.useRandom
        }

        return SimulationTask(
            simulationParams = simulationParams.copy(templateOcNet = copy),
            executionConditions = executionConditions,
            logger = logger.create(labelsActivities = copy.coreOcNet.labelsActivities),
            bindingSelector = BindingSelector(random),
            tokenSelector = TokenSelector(random),
            transitionDurationSelector = TransitionDurationSelector(
                random,
                intervalFunction = copy.intervalFunction
            ),
            transitionInstanceOccurenceDeltaSelector = TransitionInstanceOccurenceDeltaSelector(
                random,
                intervalFunction = copy.intervalFunction
            )
        )
    }
}
