package ru.misterpotz.ocgena.simulation.stepexecutor.timepn

import ru.misterpotz.ocgena.simulation.SimulationStateProvider
import ru.misterpotz.ocgena.simulation.stepexecutor.TimePNTransitionMarking
import ru.misterpotz.ocgena.simulation.stepexecutor.TimeShiftSelector
import javax.inject.Inject

class NewTimeDeltaInteractor @Inject constructor(
    private val timeShiftSelector: TimeShiftSelector,
    private val maxTimeDeltaFinder: MaxTimeDeltaFinder,
    private val timePNTransitionMarking: TimePNTransitionMarking,
    private val simulationStateProvider: SimulationStateProvider
) {
    fun generateAndShiftTimeDelta() {
        val maxPossibleTimeDelta = maxTimeDeltaFinder.findMaxPossibleTimeDelta()
        if (maxPossibleTimeDelta != null && maxPossibleTimeDelta > 0) {
            simulationStateProvider.getSimulationStepState().onHasEnabledTransitions(true)
            val timeDelta = timeShiftSelector.selectTimeDelta(maxPossibleTimeDelta)
            timePNTransitionMarking.appendClockTime(timeDelta)
        }
    }
}