package simulation

import model.ObjectMarking
import model.Time
import simulation.binding.ActiveTransitionFinisher
import simulation.binding.EnabledBinding
import simulation.binding.EnabledBindingResolverFactory
import simulation.binding.EnabledBindingsCollector

class SimulationTaskStepExecutor(
    private val ocNet: SimulatableComposedOcNet<*>,
    private val state: SimulatableComposedOcNet.State,
    private val randomBindingSelector: RandomBindingSelector,
    private val transitionFinisher: ActiveTransitionFinisher,
    private val logger: Logger
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

    fun executeStep() {
        findAndFinishEndedTransitions()

        findAndStartEnabledTransitions()

        shiftTimeToClosestTransitionFinish()
    }

    private fun findAndStartEnabledTransitions() {
        var enabledBindings: List<EnabledBinding> = bindingsCollector.findEnabledBindings()

        while (enabledBindings.isNotEmpty()) {
            val selectedBinding = randomBindingSelector.selectBinding(enabledBindings)

            val bindingWithTokens = bindingsCollector.resolveEnabledObjectBinding(selectedBinding)

            transitionTokensLocker.lockEnabledBindingTokens(bindingWithTokens)

            enabledBindings = bindingsCollector.findEnabledBindings()
        }
    }

    private fun findAndFinishEndedTransitions() {
        val tMarking = state.tMarking
        val endedTransitions = tMarking.getEndedTransitions()

        for (transition in endedTransitions) {
            transitionFinisher.finishActiveTransition(transition)
        }
    }


    private fun shiftTimeToClosestTransitionFinish() {
        val timeUntilNextFinish = resolveTimeUntilNextTransitionFinish()
        shiftActiveTransitionsByTime(timeUntilNextFinish)
    }

    private fun resolveTimeUntilNextTransitionFinish(): Time {
        val tMarking = state.tMarking
        val earliestFinishActiveTransition = tMarking.getActiveTransitionWithEarliestFinish()
        return earliestFinishActiveTransition.timeLeftUntilFinish()
    }

    private fun shiftActiveTransitionsByTime(time: Time) {
        val tMarking = state.tMarking
        tMarking.shiftByTime(time)
    }

    class StatePMarkingProvider(val state: SimulatableComposedOcNet.State) : PMarkingProvider {
        override val pMarking: ObjectMarking
            get() = state.pMarking

    }
}
