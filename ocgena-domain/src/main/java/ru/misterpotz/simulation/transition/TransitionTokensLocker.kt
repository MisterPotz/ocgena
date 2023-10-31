package ru.misterpotz.simulation.transition

import model.OngoingActivity
import model.TransitionActivitiesMarking
import simulation.Logger
import ru.misterpotz.simulation.marking.PMarkingProvider
import ru.misterpotz.simulation.state.SimulationTime
import simulation.binding.EnabledBindingWithTokens

class TransitionTokensLocker(
    private val pMarkingProvider: PMarkingProvider,
    private val tMarking: TransitionActivitiesMarking,
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

        val tMarkingValue = OngoingActivity.create(
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
        pMarkingProvider.pMarking.minus(enabledBindingWithTokens.involvedObjectTokens)
    }

    fun lockTokensAndRecordActiveTransition(enabledBindingWithTokens: EnabledBindingWithTokens) {
        lockTokensInPMarking(enabledBindingWithTokens)
        recordActiveTransition(enabledBindingWithTokens)
    }
}
