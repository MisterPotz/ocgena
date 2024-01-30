package ru.misterpotz.ocgena.timepn

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.registries.PrePlaceRegistry
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunch
import ru.misterpotz.ocgena.simulation.stepexecutor.TransitionDisabledByMarkingChecker

class TransitionDisabledByMarkingCheckerTest {

    @Test
    fun `transition checks if disabled correctly`() {
        val transitionAccessor = mockk<PrePlaceRegistry.PrePlaceAccessor> {
            every { compareTo(any()) } returns 1
        }

        val prePlaceRegistry = mockk<PrePlaceRegistry> {
            every { transitionPrePlaces(any()) } returns transitionAccessor
        }

        val globalTokenBunch = mockk<SparseTokenBunch> {
            every { tokenAmountStorage() } returns mockk()
        }

        val transitionDisabledByMarkingChecker = TransitionDisabledByMarkingChecker(
            prePlaceRegistry = prePlaceRegistry,
            globalTokenBunch = globalTokenBunch,
            transitionsRegistry = mockk()
        )

        Assertions.assertTrue(transitionDisabledByMarkingChecker.transitionIsDisabledByMarking("test"))
    }

    @Test
    fun `transition checks if enabled correctly`() {
        val transitionAccessor = mockk<PrePlaceRegistry.PrePlaceAccessor> {
            every { compareTo(any()) } returns 0
        }

        val prePlaceRegistry = mockk<PrePlaceRegistry> {
            every { transitionPrePlaces(any()) } returns transitionAccessor
        }

        val globalTokenBunch = mockk<SparseTokenBunch> {
            every { tokenAmountStorage() } returns mockk()
        }

        val transitionDisabledByMarkingChecker = TransitionDisabledByMarkingChecker(
            prePlaceRegistry = prePlaceRegistry,
            globalTokenBunch = globalTokenBunch,
            transitionsRegistry = mockk()
        )

        Assertions.assertFalse(transitionDisabledByMarkingChecker.transitionIsDisabledByMarking("test"))
    }
}