package simulation

import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import model.ObjectMarking
import simulation.binding.ActiveTransitionFinisherImpl
import simulation.binding.InputToOutputPlaceResolverFactory
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class SimulationTask(
    private val simulationParams: SimulationParams,
    private val executionConditions: ExecutionConditions,
    private val logger: Logger,
    private val randomBindingSelector: RandomBindingSelector,
) {
    private val ocNet = simulationParams.templateOcNet
    private val initialMarking = simulationParams.initialMarking
    private val duration = simulationParams.timeoutSec.toDuration(DurationUnit.SECONDS)
    private val state = ocNet.createInitialState()

    private val runningSimulatableOcNet = RunningSimulatableOcNet(ocNet, state)
    private val executionLock: Mutex = Mutex()
    private val simulationState = SimulationState()
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
        simulationState = simulationState,
        logger = logger
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

                logger.onExecutionStep(stepIndex)

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
        run()
        logger.onEnd()
        executionLock.unlock()
    }
}
