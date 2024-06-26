package ru.misterpotz.ocgena.simulation_old.stepexecutor

import ru.misterpotz.ocgena.simulation_old.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.simulation_old.collections.ObjectTokenSet
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.simulation_old.SimulationStateProvider
import ru.misterpotz.ocgena.simulation_old.Time
import ru.misterpotz.ocgena.simulation_old.binding.EnabledBinding
import ru.misterpotz.ocgena.simulation_old.binding.TIFinisher
import ru.misterpotz.ocgena.simulation_old.continuation.ExecutionContinuation
import ru.misterpotz.ocgena.simulation_old.generator.NewTokenTimeBasedGenerator
import ru.misterpotz.ocgena.simulation_old.interactors.BindingSelectionInteractor
import ru.misterpotz.ocgena.simulation_old.interactors.EnabledBindingsCollectorInteractor
import ru.misterpotz.ocgena.simulation_old.logging.SimulationDBLogger
import ru.misterpotz.ocgena.simulation_old.state.original.CurrentSimulationStateOriginal
import ru.misterpotz.ocgena.simulation_old.structure.State
import ru.misterpotz.ocgena.simulation_old.transition.TransitionInstanceCreatorFacadeOriginal

class OriginalStepExecutor(
    private val simulationStateProvider: SimulationStateProvider,
    private val bindingSelectionInteractor: BindingSelectionInteractor,
    private val transitionFinisher: TIFinisher,
    private val transitionInstanceCreatorFacadeOriginal: TransitionInstanceCreatorFacadeOriginal,
    private val simulationDBLogger: SimulationDBLogger,
    private val newTokenTimeBasedGenerator: NewTokenTimeBasedGenerator,
    private val bindingsCollector: EnabledBindingsCollectorInteractor,
    private val objectTokenSet: ObjectTokenSet,
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
    private val currentSimulationStateOriginal: CurrentSimulationStateOriginal,
) : StepExecutor {
    val state: State
        get() = simulationStateProvider.simulatableOcNetInstance().state

    val ocNet: OCNet
        get() = simulationStateProvider.simulatableOcNetInstance().ocNet

    private val ocNetOutputPlaces by lazy(LazyThreadSafetyMode.NONE) {
        ocNet.placeTypeRegistry.getOutputPlaces(ocNet.placeRegistry)
    }

    private val simulationTime get() = simulationStateProvider.getSimulationTime()
    private val simulationStepState get() = simulationStateProvider.getSimulationStepState()


    override suspend fun executeStep(executionContinuation: ExecutionContinuation) {
        findAndFinishEndedTransitions()

        removeTokensAtFinishPlace()

        generateNewTokensAndPlanNextGeneration()

        findAndStartEnabledTransitionActivities(executionContinuation)

        increaseTimeByMinimalSomethingChangingDelta()
    }

    private fun removeTokensAtFinishPlace() {
        simulationDBLogger.beforeRemovingTokensAtFinishPlace()

        for (outputPlace in ocNetOutputPlaces.places) {
            val tokensToRemove = state.pMarking[outputPlace.id]
            objectTokenRealAmountRegistry.zeroAmountAt(outputPlace.id)

            if (!tokensToRemove.isEmpty()) {
                state.pMarking.removeAllPlaceTokens(outputPlace.id)
                objectTokenSet.removeAll(tokensToRemove)
            }
        }

        simulationDBLogger.afterRemovingTokensAtFinishPlace()
    }

    private fun generateNewTokensAndPlanNextGeneration() {
        val markingToAdd = newTokenTimeBasedGenerator.generateFictiveTokensAsMarkingSchemeAndReplan()

        markingToAdd?.placesToTokens?.forEach { (petriAtomId, tokenIncrement) ->
            objectTokenRealAmountRegistry.incrementRealAmountAt(petriAtomId, tokenIncrement)
        }
    }

    private suspend fun findAndStartEnabledTransitionActivities(executionContinuation: ExecutionContinuation) {
        val foundEnabledBindings = bindingsCollector.findEnabledBindings()

        executionContinuation.updatedEnabledBindings(foundEnabledBindings)

        var enabledBindings: List<EnabledBinding> = executionContinuation.getEnabledBindings()

        simulationDBLogger.beforeStartingNewTransitions()

        simulationStepState.onHasEnabledTransitions(hasEnabledTransitions = enabledBindings.isNotEmpty())

        if (enabledBindings.isEmpty()) {
            return
        }

        while (enabledBindings.isNotEmpty()) {

            val selectedBinding = executionContinuation.selectBinding()
                ?: bindingSelectionInteractor.selectBinding(enabledBindings)

            val bindingWithTokens = bindingsCollector.resolveEnabledObjectBinding(selectedBinding)

            transitionInstanceCreatorFacadeOriginal.lockTokensAndRecordNewTransitionInstance(bindingWithTokens)

            enabledBindings = bindingsCollector.findEnabledBindings()

            executionContinuation.updatedEnabledBindings(enabledBindings)

            enabledBindings = executionContinuation.getEnabledBindings()
        }

        simulationDBLogger.afterStartingNewTransitions()
    }

    private fun findAndFinishEndedTransitions() {
        val tMarking = currentSimulationStateOriginal.tMarking
        val endedTransitions = tMarking.getAndPopEndedTransitions()

        simulationDBLogger.beforeEndingTransitions()
        for (transition in endedTransitions) {
            transitionFinisher.finishActiveTransition(transition)
        }
        simulationDBLogger.afterEndingTransitions()
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
        val tMarking = currentSimulationStateOriginal.tMarking
        val earliestFinishActiveTransition = tMarking.getActiveTransitionWithEarliestFinish()
        return earliestFinishActiveTransition?.timeLeftUntilFinish()
    }

    private fun resolveTimeUntilATransitionIsEnabled(): Time? {
        val tTimes = currentSimulationStateOriginal.tTimesMarking

        val earliestTransitionEnablingTime = tTimes.earliestNonZeroTime()
        return earliestTransitionEnablingTime
    }

    private fun shiftActiveTransitionsByTime(time: Time) {
        val tMarking = currentSimulationStateOriginal.tMarking
        tMarking.shiftByTime(time)
    }

    private fun shiftTransitionAllowedTime(time: Time) {
        val tTimes = currentSimulationStateOriginal.tTimesMarking
        tTimes.increaseSimTime(time)
    }

    private fun shiftGlobalTime(time: Time) {
        simulationTime.shiftByDelta(time)
    }
}