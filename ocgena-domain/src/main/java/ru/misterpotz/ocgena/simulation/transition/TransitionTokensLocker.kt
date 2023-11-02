package ru.misterpotz.ocgena.simulation.transition

import ru.misterpotz.simulation.logging.loggers.CurrentSimulationDelegate
import simulation.Logger
import simulation.binding.EnabledBindingWithTokens
import javax.inject.Inject

class TransitionTokensLocker @Inject constructor(
    private val activityAllowedTimeSelector: TransitionInstanceNextCreationTimeGenerator,
    private val transitionInstanceDurationGenerator: TransitionInstanceDurationGenerator,
    private val logger: Logger,
    private val transitionInstanceCreationFactory: TransitionInstanceCreationFactory,
    private val currentSimulationDelegate: CurrentSimulationDelegate,
) : CurrentSimulationDelegate by currentSimulationDelegate {
    private fun recordActiveTransition(enabledBindingWithTokens: EnabledBindingWithTokens) {
        val transition = enabledBindingWithTokens.transition

        val randomSelectedDuration = transitionInstanceDurationGenerator.newDuration(transition)

        val tMarkingValue = transitionInstanceCreationFactory.create(
            transition = transition,
            lockedObjectTokens = enabledBindingWithTokens.involvedObjectTokens,
            duration = randomSelectedDuration,
            startedAt = simGlobalTime,
            tokenSynchronizationTime = enabledBindingWithTokens.synchronizationTime
        )

        tMarking.pushTMarking(tMarkingValue)
        val newNextAllowedTime = activityAllowedTimeSelector.getNewActivityNextAllowedTime(transition)

        tTimesMarking.setNextAllowedTime(
            transition = transition,
            time = newNextAllowedTime
        )
        logger.onStartTransition(transition = tMarkingValue)
    }

    private fun lockTokensInPMarking(enabledBindingWithTokens: EnabledBindingWithTokens) {
        pMarking.minus(enabledBindingWithTokens.involvedObjectTokens)
    }

    fun lockTokensAndRecordNewTransitionInstance(enabledBindingWithTokens: EnabledBindingWithTokens) {
        lockTokensInPMarking(enabledBindingWithTokens)
        recordActiveTransition(enabledBindingWithTokens)
    }
}
