package ru.misterpotz.ocgena.simulation.transition

import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.simulation.generator.TransitionInstanceDurationGenerator
import ru.misterpotz.ocgena.simulation.generator.TransitionNextInstanceAllowedTimeGenerator
import ru.misterpotz.ocgena.simulation.logging.loggers.CurrentSimulationDelegate
import ru.misterpotz.ocgena.simulation.logging.Logger
import ru.misterpotz.ocgena.simulation.binding.EnabledBindingWithTokens
import javax.inject.Inject

class TransitionInstanceCreatorFacade @Inject constructor(
    private val activityAllowedTimeSelector: TransitionNextInstanceAllowedTimeGenerator,
    private val transitionInstanceDurationGenerator: TransitionInstanceDurationGenerator,
    private val logger: Logger,
    private val transitionInstanceCreationFactory: TransitionInstanceCreationFactory,
    private val currentSimulationDelegate: CurrentSimulationDelegate,
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
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
        for (place in enabledBindingWithTokens.involvedObjectTokens.keys) {
            val tokens = enabledBindingWithTokens.involvedObjectTokens[place].size
            objectTokenRealAmountRegistry.decreaseRealAmountAt(place, decrementValue = tokens)
        }
    }

    fun lockTokensAndRecordNewTransitionInstance(enabledBindingWithTokens: EnabledBindingWithTokens) {
        lockTokensInPMarking(enabledBindingWithTokens)
        recordActiveTransition(enabledBindingWithTokens)
    }
}
