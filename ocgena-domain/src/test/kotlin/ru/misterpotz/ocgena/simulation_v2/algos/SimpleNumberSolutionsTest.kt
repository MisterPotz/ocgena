package ru.misterpotz.ocgena.simulation_v2.algos

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.simulation_v2.NoTokenGenerator
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.NormalShuffler
import ru.misterpotz.ocgena.simulation_v2.input.SimulationInput
import ru.misterpotz.ocgena.simulation_v2.toTokenSliceAmounts
import ru.misterpotz.ocgena.simulation_v2.utils.toDefaultSim
import kotlin.random.Random

class SimpleNumberSolutionsTest {

    fun ocnet() = buildSynchronizingLomazovaExampleModel()

    @Test
    fun simpleNumberNoSolutionLomazovaArcs() {
        val model = ocnet().toDefaultSim(SimulationInput(loggingEnabled = false))

        val tokenSlice = mapOf(
            "b2" to 5,
            "o3" to 3,
            "p3" to 6,
            "t2" to 3,
        ).toTokenSliceAmounts(model)

        val normalShuffler = NormalShuffler(Random(42))
        val hasSolution =
            model.transitionBy("test_all_sync").inputArcsSolutions(tokenSlice, normalShuffler, NoTokenGenerator)
                .iterator()
                .hasNext()

        Assertions.assertEquals(false, hasSolution)
    }

    @Test
    fun simpleNumberSolutionLomazovaArcs() {
        val model = ocnet().toDefaultSim(SimulationInput(loggingEnabled = false))

        val tokenSlice = mapOf(
            "b2" to 8,
            "o3" to 3,
            "p3" to 6,
            "t2" to 2,
        ).toTokenSliceAmounts(model)

        val normalShuffler = NormalShuffler(Random(42))
        val solution =
            model.transitionBy("test_all_sync").inputArcsSolutions(tokenSlice, normalShuffler, NoTokenGenerator)
                .iterator()
                .next()

        Assertions.assertEquals(
            mapOf(
                "b2" to 6,
                "o3" to 3,
                "p3" to 6,
                "t2" to 2,
            ).toTokenSliceAmounts(model),
            solution.toTokenSlice()
        )
    }
}