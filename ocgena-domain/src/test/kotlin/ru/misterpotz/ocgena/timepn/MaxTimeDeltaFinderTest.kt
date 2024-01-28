package ru.misterpotz.ocgena.timepn

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.registries.TransitionsRegistry
import ru.misterpotz.ocgena.simulation.stepexecutor.timepn.MaxTimeDeltaFinder
import ru.misterpotz.ocgena.simulation.stepexecutor.TimePNTransitionMarking
import ru.misterpotz.ocgena.simulation.stepexecutor.TimePnTransitionData
import ru.misterpotz.ocgena.simulation.stepexecutor.TransitionDisabledByMarkingChecker

class MaxTimeDeltaFinderTest {

    companion object {
        val map = mapOf(
            "enabled_1" to mockk<TimePnTransitionData>() {
                every { timeUntilEft() } returns 0
                every { timeUntilLFT() } returns 2
            },
            "enabled_2" to mockk<TimePnTransitionData>() {
                every { timeUntilEft() } returns 2
                every { timeUntilLFT() } returns 5
            },
            "disabled_1" to mockk<TimePnTransitionData>() {
                every { timeUntilEft() } returns 10
                every { timeUntilLFT() } returns 10
            },
        )
        val disabledMap = mapOf(
            "enabled_1" to false,
            "enabled_2" to false,
            "disabled_1" to true,
        )

        val onlyDisabledMap = mapOf(
            "disabled_1" to mockk<TimePnTransitionData>() {
                every { timeUntilLFT() } returns 10
                every { timeUntilEft() } returns 10
            },
        )

        val mapCase2DifferentTransitions = mapOf(
            "enabled_1" to mockk<TimePnTransitionData>() {
                every { timeUntilEft() } returns 4
                every { timeUntilLFT() } returns 5
            },
            "enabled_2" to mockk<TimePnTransitionData>() {
                every { timeUntilEft() } returns 2
                every { timeUntilLFT() } returns 10
            },
            "disabled_1" to mockk<TimePnTransitionData>() {
                every { timeUntilEft() } returns 10
                every { timeUntilLFT() } returns 10
            },
        )
    }

    @Test
    fun `only transitions enabled by marking are taken into account`() {
        val transitionsRegistry = mockk<TransitionsRegistry> {
            every { iterable } returns map.keys.map { mockk { every { id } returns it } }
        }
        val maxTimeDeltaFinder = MaxTimeDeltaFinder(
            transitionsRegistry = transitionsRegistry,
            timePNTransitionMarking = mockk<TimePNTransitionMarking> {
                every { forTransition(any()) } answers {
                    val arg = firstArg() as PetriAtomId
                    map[arg]!!
                }
            },
            transitionDisabledByMarkingChecker = mockk<TransitionDisabledByMarkingChecker>() {
                every { transitionIsDisabledByMarking(any()) } answers {
                    val strg: String = firstArg()
                    disabledMap[strg]!!
                }
            }
        )
        Assertions.assertEquals(0..2L, maxTimeDeltaFinder.findPossibleFiringTimeRange())
    }


    @Test
    fun `only transitions enabled by marking are taken into account, eft and lft of different transitions`() {
        val transitionsRegistry = mockk<TransitionsRegistry> {
            every { iterable } returns mapCase2DifferentTransitions.keys.map { mockk { every { id } returns it } }
        }
        val maxTimeDeltaFinder = MaxTimeDeltaFinder(
            transitionsRegistry = transitionsRegistry,
            timePNTransitionMarking = mockk<TimePNTransitionMarking> {
                every { forTransition(any()) } answers {
                    val arg = firstArg() as PetriAtomId
                    mapCase2DifferentTransitions[arg]!!
                }
            },
            transitionDisabledByMarkingChecker = mockk<TransitionDisabledByMarkingChecker>() {
                every { transitionIsDisabledByMarking(any()) } answers {
                    val strg: String = firstArg()
                    disabledMap[strg]!!
                }
            }
        )
        Assertions.assertEquals(2L..5L, maxTimeDeltaFinder.findPossibleFiringTimeRange())
    }


    @Test
    fun `disabled transitions are not used`() {
        val transitionsRegistry = mockk<TransitionsRegistry> {
            every { iterable } returns onlyDisabledMap.keys.map { mockk { every { id } returns it } }
        }
        val maxTimeDeltaFinder = MaxTimeDeltaFinder(
            transitionsRegistry = transitionsRegistry,
            timePNTransitionMarking = mockk<TimePNTransitionMarking> {
                every { forTransition(any()) } answers {
                    val arg = firstArg() as PetriAtomId
                    onlyDisabledMap[arg]!!
                }
            },
            transitionDisabledByMarkingChecker = mockk<TransitionDisabledByMarkingChecker>() {
                every { transitionIsDisabledByMarking(any()) } returns true
            }
        )
        Assertions.assertEquals(null, maxTimeDeltaFinder.findPossibleFiringTimeRange())
    }
}