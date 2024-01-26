package ru.misterpotz.ocgena.timepn

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.*
import ru.misterpotz.ocgena.simulation.SimulationStateProvider
import ru.misterpotz.ocgena.simulation.config.timepn.TransitionsTimePNSpec
import ru.misterpotz.ocgena.simulation.stepexecutor.NewTimeDeltaInteractor
import ru.misterpotz.ocgena.simulation.stepexecutor.TimePNTransitionMarking
import ru.misterpotz.ocgena.simulation.stepexecutor.TimeShiftSelector

class NewTimeDeltaInteractorTest {

    companion object {
        val SELECTED_TIME = 14L
        val MAX_TIME = 25L
    }

    @Test
    fun `dont shifts time if max time is 0`() {
        val slotTime = slot<Long>()

        val timePNTransitionMarking = mockk<TimePNTransitionMarking> {
            every { appendClockTime(capture(slotTime)) } returns Unit
        }

        val simuStateProvider = mockk<SimulationStateProvider> {
            every { getSimulationStepState() } returns mockk {
                every { onHasEnabledTransitions(true) }
            }
        }
        val newTimeDeltaInteractor = NewTimeDeltaInteractor(
            timeShiftSelector = mockk {
                every { selectTimeDelta(any()) } returns 5
            },
            maxTimeDeltaFinder = mockk {
                every { findMaxPossibleTimeDelta() } returns 0
            },
            timePNTransitionMarking = timePNTransitionMarking,
            simulationStateProvider = simuStateProvider
        )

        newTimeDeltaInteractor.generateAndShiftTimeDelta()

        verify(exactly = 0) {
            simuStateProvider.getSimulationStepState()
            timePNTransitionMarking.appendClockTime(any())
        }
    }

    @Test
    fun `shifts the randomized time if max not zero`() {

        val slotTime = slot<Long>()

        val timePNTransitionMarking = mockk<TimePNTransitionMarking> {
            every { appendClockTime(capture(slotTime)) } returns Unit
        }

        val simuStateProvider = mockk<SimulationStateProvider> {
            every { getSimulationStepState() } returns mockk {
                every { onHasEnabledTransitions(true) } returns Unit
            }
        }
        val newTimeDeltaInteractor = NewTimeDeltaInteractor(
            timeShiftSelector = mockk {
                every { selectTimeDelta(any()) } returns SELECTED_TIME
            },
            maxTimeDeltaFinder = mockk {
                every { findMaxPossibleTimeDelta() } returns MAX_TIME
            },
            timePNTransitionMarking = timePNTransitionMarking,
            simulationStateProvider = simuStateProvider
        )

        newTimeDeltaInteractor.generateAndShiftTimeDelta()

        verify(exactly = 1) {
            simuStateProvider.getSimulationStepState()
            timePNTransitionMarking.appendClockTime(any())
        }
        Assertions.assertSame(slotTime.captured, SELECTED_TIME)
    }
}