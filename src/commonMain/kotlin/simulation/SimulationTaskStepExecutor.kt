package simulation

import model.ObjectMarking
import model.Time
import simulation.binding.ActiveTransitionMarkingFinisher
import simulation.binding.EnabledBinding
import simulation.binding.EnabledBindingResolverFactory
import simulation.binding.EnabledBindingsCollector
import utils.print


class SimulationState() {
    private var current: BySteps? = null

    fun onStart() {
        current = BySteps(noEnabledTransitions = false, noPlannedTransitions = false)
    }

    fun onNewStep() {
        current = BySteps()
    }

    fun onHasEnabledTransitions(hasEnabledTransitions: Boolean) {
        require(current != null && current?.noEnabledTransitions == null)
        current!!.noEnabledTransitions = !hasEnabledTransitions
    }

    fun onHasPlannedTransitions(hasPlannedTransitions: Boolean) {
        require(current != null && current?.noPlannedTransitions == null)
        current!!.noPlannedTransitions = !hasPlannedTransitions
    }

    fun isFinished(): Boolean {
        require(current != null)
        return current!!.noPlannedTransitions!! && current!!.noEnabledTransitions!!
    }

    private class BySteps(
        var noEnabledTransitions: Boolean? = null,
        var noPlannedTransitions: Boolean? = null,
    )
}

class SimulationTime(var globalTime : Time = 0) {
    fun shiftByDelta(delta : Time) {
        globalTime += delta
    }

    override fun toString(): String {
        return globalTime.print()
    }
}

class SimulationTaskStepExecutor(
    private val ocNet: SimulatableComposedOcNet<*>,
    private val state: SimulatableComposedOcNet.State,
    private val randomBindingSelector: RandomBindingSelector,
    private val transitionFinisher: ActiveTransitionMarkingFinisher,
    private val logger: Logger,
    private val simulationTime: SimulationTime,
    private val simulationState: SimulationState,
) {

    private val pMarkingProvider = StatePMarkingProvider(state = state)
    private val enabledBindingResolverFactory: EnabledBindingResolverFactory = EnabledBindingResolverFactory(
        ocNet.arcMultiplicity,
        arcs = ocNet.coreOcNet.arcs,
        pMarkingProvider = pMarkingProvider
    )
    private val transitionTokensLocker = TransitionTokensLocker(
        pMarkingProvider,
        state.tMarking,
        ocNet.intervalFunction,
        logger
    )
    private val bindingsCollector = EnabledBindingsCollector(
        transitions = ocNet.coreOcNet.transitions,
        enabledBindingResolverFactory = enabledBindingResolverFactory
    )
    private var lastStepWasFinal = false

    fun executeStep() {
        findAndFinishEndedTransitions()

        findAndStartEnabledTransitions()

        shiftTimeToClosestTransitionFinish()
    }

    private fun findAndStartEnabledTransitions() {
        val enabledBindings: List<EnabledBinding> = bindingsCollector.findEnabledBindings()

        if (enabledBindings.isEmpty()) {
            simulationState.onHasEnabledTransitions(hasEnabledTransitions = false)
            return
        }

        val selectedBinding = randomBindingSelector.selectBinding(enabledBindings)

        val bindingWithTokens = bindingsCollector.resolveEnabledObjectBinding(selectedBinding)

        transitionTokensLocker.lockTokensAndRecordActiveTransition(bindingWithTokens)

        simulationState.onHasEnabledTransitions(hasEnabledTransitions = enabledBindings.isNotEmpty())
    }

    private fun findAndFinishEndedTransitions() {
        val tMarking = state.tMarking
        val endedTransitions = tMarking.getAndPopEndedTransitions()

        logger.onTransitionEndSectionStart()
        for (transition in endedTransitions) {
            transitionFinisher.finishActiveTransition(transition)
        }
    }

    private fun shiftTimeToClosestTransitionFinish() {
        val timeUntilNextFinish = resolveTimeUntilNextTransitionFinish()

        simulationState.onHasPlannedTransitions(
            hasPlannedTransitions = timeUntilNextFinish != null
        )

        if (timeUntilNextFinish != null) {
            shiftGlobalTime(timeUntilNextFinish)
            shiftActiveTransitionsByTime(timeUntilNextFinish)
            logger.onTimeShift(timeUntilNextFinish)
        }
    }

    private fun resolveTimeUntilNextTransitionFinish(): Time? {
        val tMarking = state.tMarking
        val earliestFinishActiveTransition = tMarking.getActiveTransitionWithEarliestFinish()
        return earliestFinishActiveTransition?.timeLeftUntilFinish()
    }

    private fun shiftActiveTransitionsByTime(time: Time) {
        val tMarking = state.tMarking
        tMarking.shiftByTime(time)

    }

    private fun shiftGlobalTime(time: Time) {
        simulationTime.shiftByDelta(time)
    }

    class StatePMarkingProvider(val state: SimulatableComposedOcNet.State) : PMarkingProvider {
        override val pMarking: ObjectMarking
            get() = state.pMarking

    }
}
