package ru.misterpotz.simulation.transition

import ru.misterpotz.marking.transitions.TransitionInstance
import ru.misterpotz.marking.transitions.TransitionInstancesMarking
import ru.misterpotz.marking.transitions.TransitionTimesMarking
import simulation.Logger
import ru.misterpotz.simulation.marking.PMarkingProvider
import ru.misterpotz.simulation.state.SimulationTime
import simulation.binding.EnabledBindingWithTokens

class TransitionTokensLocker(
    private val pMarkingProvider: PMarkingProvider,
    private val tMarking: TransitionInstancesMarking,
    private val activityAllowedTimeSelector: TransitionInstanceNextCreationTimeGenerator,
    private val transitionInstanceDurationGenerator: TransitionInstanceDurationGenerator,
    private val tTimes: TransitionTimesMarking,
    private val logger: Logger,
    private val simulationTime: SimulationTime,
) {
    private fun recordActiveTransition(enabledBindingWithTokens: EnabledBindingWithTokens) {
        val transition = enabledBindingWithTokens.transition

        val randomSelectedDuration = transitionInstanceDurationGenerator.newDuration(
            transition.id
        )

        val tMarkingValue = TransitionInstance.create(
            transition = transition,
            lockedObjectTokens = enabledBindingWithTokens.involvedObjectTokens,
            duration = randomSelectedDuration,
            startedAt = simulationTime.globalTime,
            tokenSynchronizationTime = enabledBindingWithTokens.synchronizationTime
        )

        tMarking.pushTMarking(tMarkingValue)
        val newNextAllowedTime = activityAllowedTimeSelector.getNewActivityNextAllowedTime(transition.id)

        tTimes.setNextAllowedTime(
            transition = transition.id,
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
