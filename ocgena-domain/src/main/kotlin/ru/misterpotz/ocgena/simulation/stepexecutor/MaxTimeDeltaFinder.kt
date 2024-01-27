package ru.misterpotz.ocgena.simulation.stepexecutor

import ru.misterpotz.ocgena.registries.TransitionsRegistry
import ru.misterpotz.ocgena.utils.TimePNRef
import javax.inject.Inject

@TimePNRef("elapsing of time")
class MaxTimeDeltaFinder @Inject constructor(
    private val transitionsRegistry: TransitionsRegistry,
    private val timePNTransitionMarking: TimePNTransitionMarking,
    private val transitionDisabledChecker: TransitionDisabledChecker,
) {
    fun findMaxPossibleTimeDelta(): Long? {
        val partiallyEnabledTransitions = transitionsRegistry.iterable.filter { transition ->
            !transitionDisabledChecker.transitionIsDisabled(transition.id)
        }
        val minimumLftTransition = partiallyEnabledTransitions.minByOrNull { transition ->
            timePNTransitionMarking.forTransition(transition.id).timeUntilLFT()
        } ?: return null

        val transitionData = timePNTransitionMarking.forTransition(minimumLftTransition.id)

        @TimePNRef("tau")
        val timeDelta = transitionData.timeUntilLFT()
        return timeDelta
    }
}