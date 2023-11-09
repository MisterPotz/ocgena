package ru.misterpotz.ocgena.timepn

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.simulation_old.SimulationStateProvider
import ru.misterpotz.ocgena.simulation_old.stepexecutor.timepn.NewTimeDeltaInteractor
import ru.misterpotz.ocgena.simulation_old.stepexecutor.TimePNTransitionMarking
import ru.misterpotz.ocgena.simulation_old.stepexecutor.TransitionDisabledByMarkingChecker

class NewTimeDeltaInteractorTest {

    companion object {
        val SELECTED_TIME = 14L
        val MAX_TIME = 25L
    }

    private val transitionDisabledByMarkingChecker = mockk<TransitionDisabledByMarkingChecker> {
        every { transitionsPartiallyEnabledByMarking() } returns mockk()
    }

    @Test
    fun `dont shifts time if cant resolve possible firing time range`() {
        val slotTime = slot<Long>()

        val timePNTransitionMarking = mockk<TimePNTransitionMarking> {
            every { appendClockTime(any(), capture(slotTime)) } returns Unit
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
                every { findPossibleFiringTimeRange() } returns null
            },
            timePNTransitionMarking = timePNTransitionMarking,
            simulationStateProvider = simuStateProvider,
            transitionDisabledByMarkingChecker = transitionDisabledByMarkingChecker,
            simulationStepLogger = mockk(relaxed = true)
        )

        newTimeDeltaInteractor.generateAndShiftTimeDelta()

        verify(exactly = 0) {
            simuStateProvider.getSimulationStepState()
            timePNTransitionMarking.appendClockTime(any(), any())
        }
    }

    @Test
    fun `shifts time even if time range is zero to zero`() {
        val slotTime = slot<Long>()

        val timePNTransitionMarking = mockk<TimePNTransitionMarking> {
            every { appendClockTime(any(), capture(slotTime)) } returns Unit
        }

        val simuStateProvider = mockk<SimulationStateProvider> {
            every { getSimulationStepState() } returns mockk {
                every { onHasEnabledTransitions(true) } returns Unit
            }
        }
        val captSlot = slot<LongRange>()
        val zeroRange = 0..0L
        val newTimeDeltaInteractor = NewTimeDeltaInteractor(
            timeShiftSelector = mockk {
                every { selectTimeDelta(capture(captSlot)) } returns 0L
            },
            maxTimeDeltaFinder = mockk {
                every { findPossibleFiringTimeRange() } returns zeroRange
            },
            timePNTransitionMarking = timePNTransitionMarking,
            simulationStateProvider = simuStateProvider,
            transitionDisabledByMarkingChecker,
            mockk(relaxed = true)
        )

        newTimeDeltaInteractor.generateAndShiftTimeDelta()

        verify(exactly = 1) {
            simuStateProvider.getSimulationStepState()
            timePNTransitionMarking.appendClockTime(any(), any())
        }
        Assertions.assertSame(slotTime.captured, 0L)
        Assertions.assertSame(captSlot.captured, zeroRange)
    }

    @Test
    fun `shifts the randomized time if max not zero`() {

        val slotTime = slot<Long>()

        val timePNTransitionMarking = mockk<TimePNTransitionMarking> {
            every { appendClockTime(any(), capture(slotTime)) } returns Unit
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
                every { findPossibleFiringTimeRange() } returns 0..MAX_TIME
            },
            timePNTransitionMarking = timePNTransitionMarking,
            simulationStateProvider = simuStateProvider,
            transitionDisabledByMarkingChecker,
            mockk(relaxed = true)
        )

        newTimeDeltaInteractor.generateAndShiftTimeDelta()

        verify(exactly = 1) {
            simuStateProvider.getSimulationStepState()
            timePNTransitionMarking.appendClockTime(any(), any())
        }
        Assertions.assertSame(slotTime.captured, SELECTED_TIME)
    }
}