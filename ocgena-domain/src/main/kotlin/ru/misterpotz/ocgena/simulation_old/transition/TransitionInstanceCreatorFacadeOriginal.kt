package ru.misterpotz.ocgena.simulation_old.transition

import ru.misterpotz.ocgena.simulation_old.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.simulation_old.collections.PlaceToObjectMarkingDelta
import ru.misterpotz.ocgena.simulation_old.generator.original.TransitionInstanceDurationGeneratorOriginal
import ru.misterpotz.ocgena.simulation_old.generator.original.TransitionNextInstanceAllowedTimeGeneratorOriginal
import ru.misterpotz.ocgena.simulation_old.state.CurrentSimulationDelegate
import ru.misterpotz.ocgena.simulation_old.logging.SimulationDBLogger
import ru.misterpotz.ocgena.simulation_old.binding.EnabledBindingWithTokens
import ru.misterpotz.ocgena.simulation_old.state.original.CurrentSimulationStateOriginal
import javax.inject.Inject

class TransitionInstanceCreatorFacadeOriginal @Inject constructor(
    private val activityAllowedTimeSelector: TransitionNextInstanceAllowedTimeGeneratorOriginal,
    private val transitionInstanceDurationGeneratorOriginal: TransitionInstanceDurationGeneratorOriginal,
    private val simulationDBLogger: SimulationDBLogger,
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
        simulationDBLogger.onStartTransition(transition = tMarkingValue)
    }

    private fun lockTokensInPMarking(enabledBindingWithTokens: EnabledBindingWithTokens) {
        pMarking.minus(enabledBindingWithTokens.involvedObjectTokens as PlaceToObjectMarkingDelta)
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
