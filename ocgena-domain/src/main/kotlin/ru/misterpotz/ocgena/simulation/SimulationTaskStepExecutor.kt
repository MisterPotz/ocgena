package ru.misterpotz.ocgena.simulation

import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.collections.ObjectTokenSet
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.simulation.interactors.BindingSelectionInteractor
import ru.misterpotz.ocgena.simulation.interactors.EnabledBindingsCollectorInteractor
import ru.misterpotz.ocgena.simulation.generator.NewTokenTimeBasedGenerator
import ru.misterpotz.ocgena.simulation.state.SimulationStepState
import ru.misterpotz.ocgena.simulation.state.SimulationTime
import ru.misterpotz.ocgena.simulation.transition.TransitionInstanceCreatorFacade
import ru.misterpotz.ocgena.simulation.binding.EnabledBinding
import ru.misterpotz.ocgena.simulation.binding.TIFinisher
import ru.misterpotz.ocgena.simulation.di.SimulationScope
import ru.misterpotz.ocgena.simulation.structure.SimulatableOcNetInstance
import ru.misterpotz.ocgena.simulation.structure.State
import ru.misterpotz.ocgena.simulation.logging.Logger
import javax.inject.Inject

enum class Status {
    EXECUTING,
    FINISHED
}

interface SimulationStateProvider {
    val status: Status
    fun getSimulationTime(): SimulationTime
    fun getSimulationStepState(): SimulationStepState
    fun simulatableOcNetInstance(): SimulatableOcNetInstance
    fun markFinished()
}

@SimulationScope
class SimulationStateProviderImpl @Inject constructor(
    private val simulatableOcNetInstance: SimulatableOcNetInstance,
) : SimulationStateProvider {
    private val simulationTime = SimulationTime()
    private val simulationStepState = SimulationStepState()
    override var status: Status = Status.EXECUTING

    override fun getSimulationTime(): SimulationTime {
        return simulationTime
    }

    override fun getSimulationStepState(): SimulationStepState {
        return simulationStepState
    }

    override fun simulatableOcNetInstance(): SimulatableOcNetInstance {
        return simulatableOcNetInstance
    }

    override fun markFinished() {
        status = Status.FINISHED
    }
}

class SimulationTaskStepExecutor @Inject constructor(
    private val simulationStateProvider: SimulationStateProvider,
    private val bindingSelectionInteractor: BindingSelectionInteractor,
    private val transitionFinisher: TIFinisher,
    private val transitionInstanceCreatorFacade: TransitionInstanceCreatorFacade,
    private val logger: Logger,
    private val newTokenTimeBasedGenerator: NewTokenTimeBasedGenerator,
    private val bindingsCollector: EnabledBindingsCollectorInteractor,
    private val objectTokenSet: ObjectTokenSet,
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry
) {
    val state: State
        get() = simulationStateProvider.simulatableOcNetInstance().state

    val ocNet: OCNet
        get() = simulationStateProvider.simulatableOcNetInstance().ocNet

    val ocNetOutputPlaces by lazy(LazyThreadSafetyMode.NONE) {
        ocNet.placeTypeRegistry.getOutputPlaces(ocNet.placeRegistry)
    }

    private val simulationTime get() = simulationStateProvider.getSimulationTime()
    private val simulationStepState get() = simulationStateProvider.getSimulationStepState()

    fun executeStep() {
        findAndFinishEndedTransitions()

        removeTokensAtFinishPlace()

        generateNewTokensAndPlanNextGeneration()

        findAndStartEnabledTransitionActivities()

        increaseTimeByMinimalSomethingChangingDelta()
    }

    private fun removeTokensAtFinishPlace() {
        logger.beforeRemovingTokensAtFinishPlace()

        for (outputPlace in ocNetOutputPlaces.places) {
            val tokensToRemove = state.pMarking[outputPlace.id]

            if (!tokensToRemove.isNullOrEmpty()) {
                state.pMarking.removeAllPlaceTokens(outputPlace.id)
                objectTokenSet.removeAll(tokensToRemove)
            }
        }

        logger.afterRemovingTokensAtFinishPlace()
    }

    private fun generateNewTokensAndPlanNextGeneration() {
        val markingToAdd = newTokenTimeBasedGenerator.generateFictiveTokensAsMarkingSchemeAndReplan()

        markingToAdd?.placesToTokens?.forEach { (petriAtomId, tokenIncrement) ->
            objectTokenRealAmountRegistry.incrementRealAmountAt(petriAtomId, tokenIncrement)
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
            val selectedBinding = bindingSelectionInteractor.selectBinding(enabledBindings)

            val bindingWithTokens = bindingsCollector.resolveEnabledObjectBinding(selectedBinding)

            transitionInstanceCreatorFacade.lockTokensAndRecordNewTransitionInstance(bindingWithTokens)

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

    private fun increaseTimeByMinimalSomethingChangingDelta() {
        val timeUntilNextFinish = resolveTimeUntilNextTransitionFinish()
        val timeUntilNextEnabledTransition = resolveTimeUntilATransitionIsEnabled()
        val timeUntilNewTokenGenerated = resolveTimeUntilNextTokenGeneration()

        val times = listOf(timeUntilNextFinish, timeUntilNextEnabledTransition, timeUntilNewTokenGenerated)

        val minimumTime = times.filterNotNull().minOrNull()

        simulationStepState.onHasPlannedTransitions(
            hasPlannedTransitions = timeUntilNextEnabledTransition != null
        )
        simulationStepState.onHasPlannedTokens(timeUntilNewTokenGenerated != null)

        if (minimumTime != null) {
            shiftGlobalTime(minimumTime)
            shiftActiveTransitionsByTime(minimumTime)
            shiftTransitionAllowedTime(minimumTime)
            newTokenTimeBasedGenerator.increaseTime(minimumTime)
        }
    }

    private fun resolveTimeUntilNextTokenGeneration(): Time? {
        val time = newTokenTimeBasedGenerator.getTimeUntilNextPlanned()
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
}
