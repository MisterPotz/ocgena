package ru.misterpotz.ocgena.simulation_v2.algos

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.simulation_v2.NoTokenGenerator
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.NormalShuffler
import ru.misterpotz.ocgena.simulation_v2.buildTokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_selection.ModelAccessor
import ru.misterpotz.ocgena.simulation_v2.entities_storage.SimpleTokenSlice
import ru.misterpotz.ocgena.simulation_v2.input.SimulationInput
import ru.misterpotz.ocgena.simulation_v2.utils.toDefaultSim
import ru.misterpotz.ocgena.testing.buildOCNet
import kotlin.random.Random

class TransitionNoSyncArcSolverTest {
    fun ocnet() = buildOCNet {
        "input".p { input }

        "input".arc("t0".t)
        "input".arc("t1".t)
        "input".arc("t2".t)
        "input".arc("t3".t)
        "p1".p
        "p2".p
        "p3".p
        "p4".p
        "p5".p

        "t1".apply {
            arc("p1")
            arc("buffer".p) { multiplicity = 2 }.arc("t2".t)
        }
        "t2".apply {
            arc("p2")
            arc("p3")
        }

        "t0".apply {
            arc("p1")
            arc("p3")
            arc("p4")
        }

        "t3".apply {
            arc("p5")
        }

        "p1".p.arc("test".t)
        "p2".p.arc("test".t)
        "p3".p.arc("test".t)
        "p4".p.arc("test".t)
        "p5".p.arc("test".t)
        "test".t.arc("out".p { output })
    }

    private fun buildTokens(model: ModelAccessor): SimpleTokenSlice = buildTokenSlice(
        model = model,
        placeToTokens = mapOf(
            "p1" to listOf(
                // from t1
                1, 3, 5, 7, 9, 71, 73,
                // from t0
                40, 41
            ),
            "p2" to listOf(
                11, 12, 13, 10, 72, 74, 100, 102, 103, 104, 105, 106, 107, 108
            ),
            "p3" to listOf(
                // from t2
                2, 4, 6, 8, 14, 15, 16,
                // from t0
                42, 43
            ),
            "p4" to listOf(
                // from t0
                44, 45
            ),
            "p5" to listOf(
                // t3
                30, 31, 32, 33
            ),
        )
    )

    @Test
    fun print() {
        // http://magjac.com/graphviz-visual-editor/
        println(ocnet().toDot())
    }

    @Test
    fun transitionHistoryBuildTest() {
        val model = ocnet().toDefaultSim()
        val tokenSlice = buildTokens(model)

        tokenSlice.print()
        assertEquals(9, tokenSlice.tokensAt(model.place("p1")).size)
        assertEquals(14, tokenSlice.tokensAt(model.place("p2")).size)
        assertEquals(9, tokenSlice.tokensAt(model.place("p3")).size)
        assertEquals(2, tokenSlice.tokensAt(model.place("p4")).size)
        assertEquals(4, tokenSlice.tokensAt(model.place("p5")).size)
    }

    @Test
    fun modelBuildingTest() {
        val model = ocnet().toDefaultSim(
            SimulationInput(
                loggingEnabled = true
            )
        )

        model.transitionBy("test").printIndependentArcGroups()
        assertEquals(0, model.transitionBy("test").independentMultiArcConditions.size)
    }

    @Test
    fun testMultiSearchTokenSolver() {
        val model = ocnet().toDefaultSim(
            SimulationInput(
                loggingEnabled = true,
            )
        )
        val tokenSlice = buildTokens(model)

        val normalShuffler = NormalShuffler(random = Random(42))
        val solutions =
            model.transitionBy("test").inputArcsSolutions(tokenSlice, normalShuffler, NoTokenGenerator).iterator()
                .asSequence().toList()

        // combinatorics check
        assertEquals(
            tokenSlice.byPlaceIterator().asSequence().fold(1) { acc, (place, tokens) ->
                acc * tokens.size
            }.also { println(it) },
            solutions.size
        )
    }

    @Test
    fun testMultiSearchTokenSolver2() {
        val model = ocnet().toDefaultSim(
            SimulationInput(
                loggingEnabled = true,
            )
        )
        val tokenSlice = buildTokens(model)

        val normalShuffler = NormalShuffler(random = Random(42))
        val solutions =
            model.transitionBy("test").inputArcsSolutions(tokenSlice, normalShuffler, NoTokenGenerator, v2Solver = true).iterator()
                .asSequence().toList()

        // combinatorics check
        assertEquals(
            tokenSlice.byPlaceIterator().asSequence().fold(1) { acc, (place, tokens) ->
                acc * tokens.size
            }.also { println(it) },
            solutions.size
        )
    }
}