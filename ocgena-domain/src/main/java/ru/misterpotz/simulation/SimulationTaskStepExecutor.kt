package simulation

import model.ObjectMarking
import model.PlaceTyping
import model.Time
import ru.misterpotz.simulation.marking.PMarkingProvider
import ru.misterpotz.simulation.state.SimulationState
import ru.misterpotz.simulation.state.SimulationTime
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

class SimulationTaskStepExecutor @Inject constructor(
    ocNet: SimulatableComposedOcNet<*>,
    private val state: SimulatableComposedOcNet.State,
    private val bindingSelector: BindingSelector,
    tokenSelector: TokenSelector,
    transitionDurationSelector: TransitionDurationSelector,
    nextTransitionOccurenceTimeSelector: TransitionInstanceOccurenceDeltaSelector,
    private val transitionFinisher: ActiveTransitionMarkingFinisher,
    private val logger: Logger,
    private val simulationTime: SimulationTime,
    private val simulationState: SimulationState,
    val placeTyping: PlaceTyping,
    private val generationQueue: GenerationQueue,
    private val dumpState: () -> Unit
) {

    private val pMarkingProvider = StatePMarkingProvider(state = state)
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
            simulationState.onHasEnabledTransitions(hasEnabledTransitions = false)
            return
        }
        simulationState.onHasEnabledTransitions(hasEnabledTransitions = enabledBindings.isNotEmpty())

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

        simulationState.onHasPlannedTransitions(
            hasPlannedTransitions = timeUntilNextEnabledTransition != null
        )
        simulationState.onHasPlannedTokens(timeUntilNewTokenGenerated != null)

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

    private fun resolveTimeUntilATransitionIsEnabled() : Time? {
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

    class StatePMarkingProvider(val state: SimulatableComposedOcNet.State) : PMarkingProvider {
        override val pMarking: ObjectMarking
            get() = state.pMarking

    }
}
