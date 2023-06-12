package simulation

import simulation.time.TransitionInstanceOccurenceDeltaSelector
import model.ObjectMarking
import model.Time
import simulation.binding.ActiveTransitionMarkingFinisher
import simulation.binding.EnabledBinding
import simulation.binding.EnabledBindingResolverFactory
import simulation.binding.EnabledBindingsCollector
import simulation.random.BindingSelector
import simulation.random.TokenSelector
import simulation.time.TransitionDurationSelector


class SimulationTaskStepExecutor(
    private val ocNet: SimulatableComposedOcNet<*>,
    private val state: SimulatableComposedOcNet.State,
    private val bindingSelector: BindingSelector,
    private val tokenSelector: TokenSelector,
    private  val transitionDurationSelector: TransitionDurationSelector,
    private val nextTransitionOccurenceTimeSelector: TransitionInstanceOccurenceDeltaSelector,
    private val transitionFinisher: ActiveTransitionMarkingFinisher,
    private val logger: Logger,
    private val simulationTime: SimulationTime,
    private val simulationState: SimulationState,
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

        findAndStartEnabledTransitions()

        shiftByMinimalSomethingChangingTime()
        dumpState()
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
        val shiftingTime = timeUntilNextFinishA ?: resolveTimeUntilATransitionIsEnabled()


        simulationState.onHasPlannedTransitions(
            hasPlannedTransitions = shiftingTime != null
        )

        if (shiftingTime != null) {
            shiftGlobalTime(shiftingTime)
            shiftActiveTransitionsByTime(shiftingTime)
            shiftTransitionAllowedTime(shiftingTime)
            logger.onTimeShift(shiftingTime)
        }
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
