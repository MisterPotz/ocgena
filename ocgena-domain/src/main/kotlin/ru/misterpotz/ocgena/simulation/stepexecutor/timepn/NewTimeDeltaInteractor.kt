package ru.misterpotz.ocgena.simulation.stepexecutor.timepn

import ru.misterpotz.ocgena.simulation.SimulationStateProvider
import ru.misterpotz.ocgena.simulation.stepexecutor.SimulationStepLogger
import ru.misterpotz.ocgena.simulation.stepexecutor.TimePNTransitionMarking
import ru.misterpotz.ocgena.simulation.stepexecutor.TimeShiftSelector
import ru.misterpotz.ocgena.simulation.stepexecutor.TransitionDisabledByMarkingChecker
import javax.inject.Inject

class NewTimeDeltaInteractor @Inject constructor(
    private val timeShiftSelector: TimeShiftSelector,
    private val maxTimeDeltaFinder: MaxTimeDeltaFinder,
    private val timePNTransitionMarking: TimePNTransitionMarking,
    private val simulationStateProvider: SimulationStateProvider,
    private val transitionDisabledByMarkingChecker: TransitionDisabledByMarkingChecker,
    private val simulationStepLogger: SimulationStepLogger,
) {
    fun generateAndShiftTimeDelta() {
        val possibleTimeShiftRange = maxTimeDeltaFinder.findPossibleFiringTimeRange()
        if (possibleTimeShiftRange != null) {
            simulationStateProvider.getSimulationStepState().onHasEnabledTransitions(true)

            val timeDelta = timeShiftSelector.selectTimeDelta(possibleTimeShiftRange)

            // TODO: also take into consideration synchronization of tokens - only transitions that have all necessary synchronized tokens can fire
            val transitionsPartiallyEnabledByMarking = transitionDisabledByMarkingChecker.transitionsPartiallyEnabledByMarking()

            timePNTransitionMarking.appendClockTime(
                transitionsPartiallyEnabledByMarking,
                timeDelta
            )
            simulationStepLogger.logClockIncrement(timeDelta)
        }
    }
}