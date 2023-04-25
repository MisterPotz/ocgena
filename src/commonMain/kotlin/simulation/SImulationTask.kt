package simulation

import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import simulation.binding.ActiveTransitionFinisherImpl
import simulation.binding.InputToOutputPlaceResolverFactory
import simulation.random.BindingSelector
import simulation.random.TokenSelector
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class SimulationTask(
    private val simulationParams: SimulationParams,
    private val executionConditions: ExecutionConditions,
    private val logger: Logger,
    private val bindingSelector: BindingSelector,
    private val tokenSelector : TokenSelector,
) {
    private val ocNet = simulationParams.templateOcNet
    private val simulationTime = SimulationTime()
    private val initialMarking = simulationParams.initialMarking
    private val duration = (simulationParams.timeoutSec ?: 30L).toDuration(DurationUnit.SECONDS)
    private val state = ocNet.createInitialState()


    private val runningSimulatableOcNet = RunningSimulatableOcNet(ocNet, state)
    private val executionLock: Mutex = Mutex()
    private val simulationState = SimulationState()
    private val stepExecutor = SimulationTaskStepExecutor(
        ocNet,
        state,
        bindingSelector,
        transitionFinisher = ActiveTransitionFinisherImpl(
            state.pMarking,
            inputToOutputPlaceResolver = InputToOutputPlaceResolverFactory(
                arcMultiplicity = ocNet.arcMultiplicity,
                arcs = ocNet.coreOcNet.arcs
            ).create(),
            logger
        ),
        simulationState = simulationState,
        simulationTime = simulationTime,
        logger = logger,
        tokenSelector = tokenSelector
    )

    private fun prepare() {
        ocNet.initialize()

        state.pMarking += initialMarking
    }

    private suspend fun run() {
        var stepIndex: Int = 0

        simulationState.onStart()

        withTimeout(duration) {
            while (
                !executionConditions.checkTerminateConditionSatisfied(runningSimulatableOcNet)
                && !simulationState.isFinished()
                && isActive
            ) {

                simulationState.onNewStep()

                logger.onExecutionStepStart(stepIndex, state, simulationTime)

                stepExecutor.executeStep()

                stepIndex++

//            logger.logBindingExecution(selectedBinding)
//            executionConditions.checkIfSuspend()
            }

            if (!this.isActive) {
                logger.onTimeout()
            }
        }

    }

    suspend fun prepareAndRun() {
        if (executionLock.isLocked) return
        executionLock.lock()
        logger.onStart()
        prepare()
        logger.onInitialMarking(state.pMarking)
        run()
        logger.onFinalMarking(state.pMarking)
        logger.onEnd()
        executionLock.unlock()
    }
}
