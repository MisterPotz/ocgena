package ru.misterpotz.ocgena.simulation.stepexecutor.timepn

import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.registries.TransitionsRegistry
import ru.misterpotz.ocgena.simulation.stepexecutor.TimePNTransitionMarking
import ru.misterpotz.ocgena.simulation.stepexecutor.TransitionDisabledByMarkingChecker
import ru.misterpotz.ocgena.utils.TimePNRef
import javax.inject.Inject

@TimePNRef("elapsing of time")
class MaxTimeDeltaFinder @Inject constructor(
    private val transitionsRegistry: TransitionsRegistry,
    private val timePNTransitionMarking: TimePNTransitionMarking,
    private val transitionDisabledByMarkingChecker: TransitionDisabledByMarkingChecker,
) {
    fun findPossibleFiringTimeRange(): LongRange? {
        val partiallyEnabledTransitions = transitionsRegistry.iterable.filter { transition ->
            !transitionDisabledByMarkingChecker.transitionIsDisabledByMarking(transition.id)
        }
        if (partiallyEnabledTransitions.isEmpty())
            return null

        val minimumLftTransition = partiallyEnabledTransitions.minByOrNull { transition ->
            timePNTransitionMarking.forTransition(transition.id).timeUntilLFT()
        }!!
        val minimumEftTransition = partiallyEnabledTransitions.minByOrNull { transition: Transition ->
            timePNTransitionMarking.forTransition(transition.id).timeUntilEft()
        }!!

        val earliestlftTransition = timePNTransitionMarking.forTransition(minimumLftTransition.id)
        val earliestEftTransition = timePNTransitionMarking.forTransition(minimumEftTransition.id)

        return (earliestEftTransition.timeUntilEft())..(earliestlftTransition.timeUntilLFT())
    }
}