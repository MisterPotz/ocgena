package simulation

import model.ObjectMarking
import model.Time
import ru.misterpotz.simulation.config.SimulationConfig
import ru.misterpotz.simulation.marking.PMarkingProvider
import ru.misterpotz.simulation.state.SimulationStepState
import ru.misterpotz.simulation.state.SimulationTime
import ru.misterpotz.simulation.structure.RunningSimulatableOcNet
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet
import ru.misterpotz.simulation.transition.TransitionDurationSelector
import ru.misterpotz.simulation.transition.TransitionInstanceOccurenceDeltaSelector
import ru.misterpotz.simulation.transition.TransitionTokensLocker
import simulation.binding.ActiveTransitionMarkingFinisher
import simulation.binding.EnabledBinding
import simulation.binding.EnabledBindingResolverFactory
import simulation.binding.EnabledBindingsCollector
import simulation.random.BindingSelector
import simulation.random.TokenSelector
import javax.inject.Inject

interface SimulationTaskStepExecutorFactory {
    fun createSimulationTaskStepExecutor(dumpStateCallback: () -> Unit): SimulationTaskStepExecutor
}

class SimulationTaskStepExecutorFactoryImpl @Inject constructor(
    private val simulationConfig: SimulationConfig,
    private val simulationStateProvider: SimulationStateProvider,
    private val bindingSelector: BindingSelector,
    private val tokenSelector: TokenSelector,
    private val transitionDurationSelector: TransitionDurationSelector,
    private val nextTransitionOccurenceTimeSelector: TransitionInstanceOccurenceDeltaSelector,
    private val pMarkingProvider: SimulationTaskStepExecutor.StatePMarkingProvider,
    private val transitionFinisher: ActiveTransitionMarkingFinisher,
    private val logger: Logger,
    private val generationQueue: GenerationQueue,
) : SimulationTaskStepExecutorFactory {
    override fun createSimulationTaskStepExecutor(dumpState: () -> Unit): SimulationTaskStepExecutor {
        return SimulationTaskStepExecutor(
            simulationConfig = simulationConfig,
            simulationStateProvider = simulationStateProvider,
            bindingSelector = bindingSelector,
            tokenSelector = tokenSelector,
            transitionDurationSelector = transitionDurationSelector,
            nextTransitionOccurenceTimeSelector = nextTransitionOccurenceTimeSelector,
            pMarkingProvider = pMarkingProvider,
            transitionFinisher = transitionFinisher,
            logger = logger,
            generationQueue = generationQueue,
            dumpState = dumpState
        )
    }

}

enum class Status {
    EXECUTING,
    FINISHED
}

interface SimulationStateProvider {
    val status: Status
    fun getOcNetState(): SimulatableComposedOcNet.State
    fun getSimulationTime(): SimulationTime
    fun getSimulationStepState(): SimulationStepState
    fun runningSimulatableOcNet(): RunningSimulatableOcNet
    fun markFinished()
}

class SimulationStateProviderImpl @Inject constructor(
    private val simulationConfig: SimulationConfig
) : SimulationStateProvider {
    val state = simulationConfig.templateOcNet.createInitialState()
    val simulationTime = SimulationTime()
    private val simulationStepState = SimulationStepState()
    private val runningSimulatableOcNet = RunningSimulatableOcNet(simulationConfig.templateOcNet, state)
    override var status: Status = Status.EXECUTING

    override fun getOcNetState(): SimulatableComposedOcNet.State {
        return state
    }


    override fun getSimulationTime(): SimulationTime {
        return simulationTime
    }

    override fun getSimulationStepState(): SimulationStepState {
        return simulationStepState
    }

    override fun runningSimulatableOcNet(): RunningSimulatableOcNet {
        return runningSimulatableOcNet
    }

    override fun markFinished() {
        status = Status.FINISHED
    }
}

class SimulationTaskStepExecutor @Inject constructor(
    simulationConfig: SimulationConfig,
    private val simulationStateProvider: SimulationStateProvider,
    private val bindingSelector: BindingSelector,
    tokenSelector: TokenSelector,
    transitionDurationSelector: TransitionDurationSelector,
    nextTransitionOccurenceTimeSelector: TransitionInstanceOccurenceDeltaSelector,
    pMarkingProvider: StatePMarkingProvider,
    private val transitionFinisher: ActiveTransitionMarkingFinisher,
    private val logger: Logger,
    private val generationQueue: GenerationQueue,
    private val dumpState: () -> Unit
) {
    val ocNet = simulationConfig.templateOcNet
    val state: SimulatableComposedOcNet.State
        get() = simulationStateProvider.getOcNetState()
    val simulationTime get() = simulationStateProvider.getSimulationTime()
    val simulationStepState get() = simulationStateProvider.getSimulationStepState()

    val placeTyping get() = ocNet.coreOcNet.placeTyping

    private val enabledBindingResolverFactory: EnabledBindingResolverFactory = EnabledBindingResolverFactory(
        ocNet.arcMultiplicity,
        arcs = ocNet.coreOcNet.arcs,
        pMarkingProvider = pMarkingProvider,
        tokenSelector = tokenSelector,
        tTimes = state.tTimes
    )
    private val transitionTokensLocker = TransitionTokensLocker(
        pMarkingProvider,
        state.tMarking,
        logger = logger,
        tTimes = state.tTimes,
        transitionDurationSelector = transitionDurationSelector,
        simulationTime = simulationTime,
        transitionInstanceOccurenceDeltaSelector = nextTransitionOccurenceTimeSelector,
    )
    private val bindingsCollector = EnabledBindingsCollector(
        transitions = ocNet.coreOcNet.transitions,
        enabledBindingResolverFactory = enabledBindingResolverFactory
    )

    fun executeStep() {
        findAndFinishEndedTransitions()

        generateNewTokensAndPlanNextGeneration()

        findAndStartEnabledTransitions()

        shiftByMinimalSomethingChangingTime()
        dumpState()
    }

    private fun generateNewTokensAndPlanNextGeneration() {
        val markingToAdd = generationQueue.generateTokensAsMarkingAndReplan()
        if (markingToAdd != null) {
            state.pMarking += markingToAdd
        }
    }

    private fun findAndStartEnabledTransitions() {
        var enabledBindings: List<EnabledBinding> = bindingsCollector.findEnabledBindings()

        logger.onTransitionStartSectionStart()

        if (enabledBindings.isEmpty()) {
            simulationStepState.onHasEnabledTransitions(hasEnabledTransitions = false)
            return
        }
        simulationStepState.onHasEnabledTransitions(hasEnabledTransitions = enabledBindings.isNotEmpty())

        while (enabledBindings.isNotEmpty()) {
            val selectedBinding = bindingSelector.selectBinding(enabledBindings)

            val bindingWithTokens = bindingsCollector.resolveEnabledObjectBinding(selectedBinding)

            transitionTokensLocker.lockTokensAndRecordActiveTransition(bindingWithTokens)

            enabledBindings = bindingsCollector.findEnabledBindings()
        }
    }

    private fun findAndFinishEndedTransitions() {
        val tMarking = state.tMarking
        val endedTransitions = tMarking.getAndPopEndedTransitions()

        logger.onTransitionEndSectionStart()
        for (transition in endedTransitions) {
            transitionFinisher.finishActiveTransition(transition)
        }
    }

    private fun shiftByMinimalSomethingChangingTime() {
        val timeUntilNextFinishA = resolveTimeUntilNextTransitionFinish()
        val timeUntilNextEnabledTransition = resolveTimeUntilATransitionIsEnabled()
        val timeUntilNewTokenGenerated = resolveTimeUntilNextTokenGeneration()

        val times = listOf(timeUntilNextFinishA, timeUntilNextEnabledTransition, timeUntilNewTokenGenerated)

        val minimumTime = times.filterNotNull().minOrNull()

        simulationStepState.onHasPlannedTransitions(
            hasPlannedTransitions = timeUntilNextEnabledTransition != null
        )
        simulationStepState.onHasPlannedTokens(timeUntilNewTokenGenerated != null)

        if (minimumTime != null) {
            shiftGlobalTime(minimumTime)
            shiftActiveTransitionsByTime(minimumTime)
            shiftTransitionAllowedTime(minimumTime)
            generationQueue.shiftTime(minimumTime)
            logger.onTimeShift(minimumTime)
        }
    }

    private fun resolveTimeUntilNextTokenGeneration(): Time? {

        val time = generationQueue.getTimeUntilNextPlanned()
        return time
    }

    private fun resolveTimeUntilNextTransitionFinish(): Time? {
        val tMarking = state.tMarking
        val earliestFinishActiveTransition = tMarking.getActiveTransitionWithEarliestFinish()
        return earliestFinishActiveTransition?.timeLeftUntilFinish()
    }

    private fun resolveTimeUntilATransitionIsEnabled(): Time? {
        val tTimes = state.tTimes

        val earliestTransitionEnablingTime = tTimes.earliestNonZeroTime()
        return earliestTransitionEnablingTime
    }

    private fun shiftActiveTransitionsByTime(time: Time) {
        val tMarking = state.tMarking
        tMarking.shiftByTime(time)
    }

    private fun shiftTransitionAllowedTime(time: Time) {
        val tTimes = state.tTimes
        tTimes.shiftByTime(time)
    }

    private fun shiftGlobalTime(time: Time) {
        simulationTime.shiftByDelta(time)
    }

    class StatePMarkingProvider @Inject constructor(private val stateProvider: SimulationStateProvider) :
        PMarkingProvider {
        override val pMarking: ObjectMarking
            get() = stateProvider.getOcNetState().pMarking

    }
}
