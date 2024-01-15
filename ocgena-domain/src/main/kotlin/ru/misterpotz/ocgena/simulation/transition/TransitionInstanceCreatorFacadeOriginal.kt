package ru.misterpotz.ocgena.simulation.transition

import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.simulation.generator.original.TransitionInstanceDurationGeneratorOriginal
import ru.misterpotz.ocgena.simulation.generator.original.TransitionNextInstanceAllowedTimeGeneratorOriginal
import ru.misterpotz.ocgena.simulation.state.CurrentSimulationDelegate
import ru.misterpotz.ocgena.simulation.logging.Logger
import ru.misterpotz.ocgena.simulation.binding.EnabledBindingWithTokens
import ru.misterpotz.ocgena.simulation.state.original.CurrentSimulationStateOriginal
import javax.inject.Inject

class TransitionInstanceCreatorFacadeOriginal @Inject constructor(
    private val activityAllowedTimeSelector: TransitionNextInstanceAllowedTimeGeneratorOriginal,
    private val transitionInstanceDurationGeneratorOriginal: TransitionInstanceDurationGeneratorOriginal,
    private val logger: Logger,
    private val transitionInstanceCreationFactory: TransitionInstanceCreationFactory,
    private val currentSimulationDelegate: CurrentSimulationDelegate,
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
    private val currentSimulationStateOriginal: CurrentSimulationStateOriginal
) : CurrentSimulationDelegate by currentSimulationDelegate {
    private fun recordActiveTransition(enabledBindingWithTokens: EnabledBindingWithTokens) {
        val transition = enabledBindingWithTokens.transition

        val randomSelectedDuration = transitionInstanceDurationGeneratorOriginal.newDuration(transition)

        val tMarkingValue = transitionInstanceCreationFactory.create(
            transition = transition,
            lockedObjectTokens = enabledBindingWithTokens.involvedObjectTokens,
            duration = randomSelectedDuration,
            startedAt = simGlobalTime,
            tokenSynchronizationTime = enabledBindingWithTokens.synchronizationTime
        )

        currentSimulationStateOriginal.tMarking.pushTMarking(tMarkingValue)
        val newNextAllowedTime = activityAllowedTimeSelector.getNewActivityNextAllowedTime(transition)

        currentSimulationStateOriginal.tTimesMarking.setNextAllowedTime(
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
