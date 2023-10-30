package simulation

import ru.misterpotz.simulation.config.SimulationConfig
import ru.misterpotz.simulation.transition.TransitionDurationSelector
import ru.misterpotz.simulation.transition.TransitionInstanceOccurenceDeltaSelector
import simulation.random.BindingSelector
import simulation.random.RandomFactory
import simulation.random.TokenSelector
import kotlin.random.Random


class SimulationCreator(
    private val simulationParams: SimulationConfig,
    private val executionConditions: ExecutionConditions,
    private val logger: LoggerFactory = LoggerFactoryDefault,
    private val dumpState: Boolean = false,
    private val random: Random,
    private val bindingSelector: BindingSelector,
    private val tokenSelector: TokenSelector
) {
    fun createSimulationTask(): SimulationTask {
        val copy = simulationParams.templateOcNet
        val random = random

        return SimulationTask(
            simulationParams = simulationParams,
            executionConditions = executionConditions,
            logger = logger.create(labelMapping = simulationParams.labelMapping),
            bindingSelector = bindingSelector,
            tokenSelector = tokenSelector,
            transitionDurationSelector = TransitionDurationSelector(
                random,
                intervalFunction = copy.intervalFunction
            ),
            transitionInstanceOccurenceDeltaSelector = TransitionInstanceOccurenceDeltaSelector(
                random,
                intervalFunction = copy.intervalFunction
            ),
            tokenNextTimeSelector = TokenGenerationTimeSelector(random),
            dumpState = dumpState,
        )
    }
}
