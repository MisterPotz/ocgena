package ru.misterpotz.ocgena.simulation.stepexecutor.timepn

import ru.misterpotz.ocgena.simulation.stepexecutor.TimePNTransitionMarking
import ru.misterpotz.ocgena.simulation.stepexecutor.TransitionDisabledByMarkingChecker
import ru.misterpotz.ocgena.utils.TimePNRef
import javax.inject.Inject

@TimePNRef("elapsing of time")
class MaxTimeDeltaFinder @Inject constructor(
    private val timePNTransitionMarking: TimePNTransitionMarking,
    private val transitionDisabledByMarkingChecker: TransitionDisabledByMarkingChecker,
) {
    fun findPossibleFiringTimeRange(): LongRange? {
        val partiallyEnabledTransitions = transitionDisabledByMarkingChecker.transitionsPartiallyEnabledByMarking()
        if (partiallyEnabledTransitions.isEmpty())
            return null

        val minimumLftTransition = partiallyEnabledTransitions.minByOrNull { transition ->
            timePNTransitionMarking.forTransition(transition).timeUntilLFT()
        }!!
        val minimumEftTransition = partiallyEnabledTransitions.minByOrNull { transition->
            timePNTransitionMarking.forTransition(transition).timeUntilEft()
        }!!

        val earliestlftTransition = timePNTransitionMarking.forTransition(minimumLftTransition)
        val earliestEftTransition = timePNTransitionMarking.forTransition(minimumEftTransition)

        return (earliestEftTransition.timeUntilEft())..(earliestlftTransition.timeUntilLFT())
    }
}