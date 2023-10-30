package ru.misterpotz.simulation.transition

import model.ActiveFiringTransition
import model.ActiveTransitionMarking
import simulation.Logger
import ru.misterpotz.simulation.marking.PMarkingProvider
import ru.misterpotz.simulation.state.SimulationTime
import simulation.binding.EnabledBindingWithTokens

class TransitionTokensLocker(
    private val pMarkingProvider: PMarkingProvider,
    private val tMarking: ActiveTransitionMarking,
    private val transitionInstanceOccurenceDeltaSelector: TransitionInstanceOccurenceDeltaSelector,
    private val transitionDurationSelector: TransitionDurationSelector,
    private val tTimes: TransitionOccurrenceAllowedTimes,
    private val logger: Logger,
    private val simulationTime: SimulationTime,
) {
    private fun recordActiveTransition(enabledBindingWithTokens: EnabledBindingWithTokens) {
        val transition = enabledBindingWithTokens.transition

        val randomSelectedDuration = transitionDurationSelector.newDuration(
            transition
        )

        val tMarkingValue = ActiveFiringTransition.create(
            transition = transition,
            lockedObjectTokens = enabledBindingWithTokens.involvedObjectTokens,
            duration = randomSelectedDuration,
            startedAt = simulationTime.globalTime,
            tokenSynchronizationTime = enabledBindingWithTokens.synchronizationTime
        )

        tMarking.pushTMarking(tMarkingValue)
        val newNextAllowedTime = transitionInstanceOccurenceDeltaSelector.getNewNextOccurrenceTime(transition)

        tTimes.setNextAllowedTime(
            transition = transition,
            time = newNextAllowedTime
        )
        logger.onStartTransition(transition = tMarkingValue)
    }

    private fun lockTokensInPMarking(enabledBindingWithTokens: EnabledBindingWithTokens) {
        pMarkingProvider.pMarking -= enabledBindingWithTokens.involvedObjectTokens
    }

    fun lockTokensAndRecordActiveTransition(enabledBindingWithTokens: EnabledBindingWithTokens) {
        lockTokensInPMarking(enabledBindingWithTokens)
        recordActiveTransition(enabledBindingWithTokens)
    }
}
