package simulation

import kotlinx.coroutines.sync.Mutex
import simulation.binding.ActiveTransitionFinisherImpl
import simulation.binding.InputToOutputPlaceResolverFactory

class SimulationTask(
    private val ocNet: SimulatableComposedOcNet<*>,
    private val executionConditions: ExecutionConditions,
    private val logger: Logger,
    private val randomBindingSelector: RandomBindingSelector,
) {
    val state = ocNet.createInitialState()
    private val runningSimulatableOcNet = RunningSimulatableOcNet(ocNet, state)
    private val executionLock: Mutex = Mutex()
    private val stepExecutor = SimulationTaskStepExecutor(
        ocNet,
        state,
        randomBindingSelector,
        transitionFinisher = ActiveTransitionFinisherImpl(
            state.pMarking,
            inputToOutputPlaceResolver = InputToOutputPlaceResolverFactory(
                arcMultiplicity = ocNet.arcMultiplicity,
                arcs = ocNet.coreOcNet.arcs
            ).create(),
            logger
        ),
        logger = logger
    )

    private fun prepare() {
        ocNet.initialize()
    }

    private suspend fun run() {
        var stepIndex: Int = 0

        while (!executionConditions.checkTerminateConditionSatisfied(runningSimulatableOcNet)) {
            logger.onExecutionStep(stepIndex)
            stepExecutor.executeStep()
//            logger.logBindingExecution(selectedBinding)
//            executionConditions.checkIfSuspend()
        }
    }

    suspend fun prepareAndRun() {
        if (executionLock.isLocked) return
        executionLock.lock()
        logger.onStart()
        prepare()
        run()
        logger.onEnd()
        executionLock.unlock()
    }
}
