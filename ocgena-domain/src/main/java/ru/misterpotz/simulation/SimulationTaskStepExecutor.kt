package simulation

import ru.misterpotz.model.marking.ObjectMarking
import ru.misterpotz.model.marking.Time
import ru.misterpotz.simulation.config.SimulationConfig
import ru.misterpotz.simulation.marking.PMarkingProvider
import ru.misterpotz.simulation.queue.GenerationQueue
import ru.misterpotz.simulation.state.SimulationStepState
import ru.misterpotz.simulation.state.SimulationTime
import ru.misterpotz.simulation.structure.RunningSimulatableOcNet
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet
import ru.misterpotz.simulation.transition.TransitionInstanceDurationGenerator
import ru.misterpotz.simulation.transition.TransitionInstanceNextCreationTimeGenerator
import ru.misterpotz.simulation.transition.TransitionTokensLocker
import simulation.binding.TransitionInstanceFinisher
import simulation.binding.EnabledBinding
import simulation.binding.EnabledBindingsCollector
import simulation.random.BindingSelector
import simulation.random.TokenSelector
import javax.inject.Inject

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
    transitionInstanceDurationGenerator: TransitionInstanceDurationGenerator,
    nextTransitionOccurenceTimeSelector: TransitionInstanceNextCreationTimeGenerator,
    pMarkingProvider: StatePMarkingProvider,
    private val transitionFinisher: TransitionInstanceFinisher,
    private val logger: Logger,
    private val generationQueue: GenerationQueue,
    private val bindingsCollector: EnabledBindingsCollector,
) {
    val ocNet = simulationConfig.templateOcNet
    val state: SimulatableComposedOcNet.State
        get() = simulationStateProvider.getOcNetState()
    val simulationTime get() = simulationStateProvider.getSimulationTime()
    val simulationStepState get() = simulationStateProvider.getSimulationStepState()

    val placeTyping get() = ocNet.coreOcNet.placeTyping

    private val transitionTokensLocker = TransitionTokensLocker(
        pMarkingProvider,
        state.tMarking,
        logger = logger,
        tTimes = state.tTimesMarking,
        transitionInstanceDurationGenerator = transitionInstanceDurationGenerator,
        simulationTime = simulationTime,
        activityAllowedTimeSelector = nextTransitionOccurenceTimeSelector,
    )

    fun executeStep() {
        findAndFinishEndedTransitions()

        generateNewTokensAndPlanNextGeneration()

        findAndStartEnabledTransitionActivities()

        shiftByMinimalSomethingChangingTime()
    }

    private fun generateNewTokensAndPlanNextGeneration() {
        val markingToAdd = generationQueue.generateTokensAsMarkingAndReplan()
        if (markingToAdd != null) {
            state.pMarking.plus(markingToAdd)
        }
    }

    private fun findAndStartEnabledTransitionActivities() {
        var enabledBindings: List<EnabledBinding> = bindingsCollector.findEnabledBindings()

        logger.beforeStartingNewTransitions()

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

        logger.afterStartingNewTransitions()
    }

    private fun findAndFinishEndedTransitions() {
        val tMarking = state.tMarking
        val endedTransitions = tMarking.getAndPopEndedTransitions()

        logger.beforeEndingTransitions()
        for (transition in endedTransitions) {
            transitionFinisher.finishActiveTransition(transition)
        }
        logger.afterEndingTransitions()
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
        val tTimes = state.tTimesMarking

        val earliestTransitionEnablingTime = tTimes.earliestNonZeroTime()
        return earliestTransitionEnablingTime
    }

    private fun shiftActiveTransitionsByTime(time: Time) {
        val tMarking = state.tMarking
        tMarking.shiftByTime(time)
    }

    private fun shiftTransitionAllowedTime(time: Time) {
        val tTimes = state.tTimesMarking
        tTimes.increaseSimTime(time)
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
