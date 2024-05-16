package ru.misterpotz.ocgena.timepn

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.ocnet.primitives.InputArcMultiplicityDynamic
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.registries.ArcsMultiplicityRegistry
import ru.misterpotz.ocgena.registries.PrePlaceRegistry
import ru.misterpotz.ocgena.simulation_old.interactors.TokenSelectionInteractor
import ru.misterpotz.ocgena.simulation_old.stepexecutor.SparseTokenBunch
import ru.misterpotz.ocgena.simulation_old.stepexecutor.TransitionTokenSelector

internal class TransitionTokenSelectorTest {

    companion object {
        val arc1 = "p1".arcIdTo("test")
        val arc2 = "p2".arcIdTo("test")
        val arc3 = "p3".arcIdTo("test")

        val enoughMap = mapOf(
            arc1 to mockk<InputArcMultiplicityDynamic> {
                every { inputPlaceHasEnoughTokens(any()) } returns true
                every { requiredTokenAmount(any()) } returns 2
            },
            arc2 to mockk<InputArcMultiplicityDynamic> {
                every { inputPlaceHasEnoughTokens(any()) } returns true
                every { requiredTokenAmount(any()) } returns 1
            },
            arc3 to mockk<InputArcMultiplicityDynamic> {
                every { inputPlaceHasEnoughTokens(any()) } returns true
                every { requiredTokenAmount(any()) } returns 3
            }
        )

        val tokenMap = mapOf(
            arc1 to TokenSelectionInteractor.SelectedAndGeneratedTokens(sortedSetOf(1, 3), sortedSetOf(1)),
            arc2 to TokenSelectionInteractor.SelectedAndGeneratedTokens(sortedSetOf(2), sortedSetOf()),
            arc3 to TokenSelectionInteractor.SelectedAndGeneratedTokens(sortedSetOf(4, 5, 7), sortedSetOf(7))
        )
    }

    @Test
    fun `the selected tokens are recorded into the output`() {
        val transitionPrePlaceAccessor: PrePlaceRegistry.PrePlaceAccessor = mockk {
            every { transitionId } returns "test"
            every { iterator() } returns listOf("p1", "p2", "p3").iterator()
        }

        val arcsMultiplicityRegistry: ArcsMultiplicityRegistry = mockk {
            every { transitionInputMultiplicityDynamic(any()) } answers {
                val arc = firstArg<String>()
                enoughMap[arc]!!
            }
        }
        val tokenBunch = mockk<SparseTokenBunch> {
            every { tokenAmountStorage() } returns mockk()
        }
        val tokenSelectionInteractor: TokenSelectionInteractor = mockk {
            every { selectAndInitializeTokensFromPlace(any(), any(), any()) } answers {
                val place = firstArg<String>()
                tokenMap[place.arcIdTo("test")]!!
            }
        }

        val transitionTokenSelector = TransitionTokenSelector(
            transitionPrePlaceAccessor,
            arcsMultiplicityRegistry,
            tokenSelectionInteractor = tokenSelectionInteractor
        )

        val selectedAndInitialized = transitionTokenSelector.selectAndInstantiateFrom(tokenBunch)

        Assertions.assertEquals(
            tokenMap.values.flatMap { it.selected }.toSet(),
            selectedAndInitialized
                .first
                .objectMarking()
                .tokensIterator
                .asSequence()
                .toSet(),
        )
    }
}
