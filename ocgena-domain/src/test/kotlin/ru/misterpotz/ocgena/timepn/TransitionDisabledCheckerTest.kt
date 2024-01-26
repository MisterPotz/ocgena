package ru.misterpotz.ocgena.timepn

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.registries.PrePlaceRegistry
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunch
import ru.misterpotz.ocgena.simulation.stepexecutor.TransitionDisabledChecker

class TransitionDisabledCheckerTest {

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

        val transitionDisabledChecker = TransitionDisabledChecker(
            prePlaceRegistry = prePlaceRegistry,
            globalTokenBunch = globalTokenBunch
        )

        Assertions.assertTrue(transitionDisabledChecker.transitionIsDisabled("test"))
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

        val transitionDisabledChecker = TransitionDisabledChecker(
            prePlaceRegistry = prePlaceRegistry,
            globalTokenBunch = globalTokenBunch
        )

        Assertions.assertFalse(transitionDisabledChecker.transitionIsDisabled("test"))
    }
}