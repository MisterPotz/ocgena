package ru.misterpotz.ocgena.simulation_v2.algos

import net.bytebuddy.implementation.bind.annotation.IgnoreForBinding
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.simulation_v2.NoTokenGenerator
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.TransitionSynchronizationArcSolver
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.NormalShuffler
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.TransitionSyncV2FullSolutionFinder
import ru.misterpotz.ocgena.simulation_v2.entities_selection.ModelAccessor
import ru.misterpotz.ocgena.simulation_v2.entities_storage.SimpleTokenSlice
import ru.misterpotz.ocgena.simulation_v2.input.SimulationInput
import ru.misterpotz.ocgena.simulation_v2.input.SynchronizedArcGroup
import ru.misterpotz.ocgena.simulation_v2.input.TransitionSetting
import ru.misterpotz.ocgena.simulation_v2.utils.toDefaultSim
import ru.misterpotz.ocgena.testing.buildOCNet
import ru.misterpotz.ocgena.utils.print
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class TransitionSynchronizationArcSolverTest {
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

    private fun buildTransitionHistory(model: ModelAccessor): SimpleTokenSlice =
        ru.misterpotz.ocgena.simulation_v2.buildTransitionHistory(
            model = model,
            transitionToHistoryEntries = mapOf(
                "t1" to listOf(
                    listOf(1, 2, 100), // p1, p3, p2, buff2, buff2
                    listOf(3, 4, 102), // p1, p3, p2, buff2
                    listOf(5, 6, 103), // p1, p3, p2, buff2, buff2
                    listOf(7, 8, 104), // p1, p3, p2, buff2, buff2
                    listOf(9, 10, 105), // p1, p2, p2, buff2, buff2, buff2
                    listOf(71, 72, 106), // p1, p2, p2, buff2
                    listOf(73, 74, 107), // p1, p2, p2, buff2
                ),
                "t2" to listOf(
                    listOf(2, 11), // p3, p2
                    listOf(4, 12), // p3, p2
                    listOf(6, 13), // p3, p2
                    listOf(8, 108), // p3, p2
                    listOf(14, 10), // p3, p2
                    listOf(15, 72), // p3, p2
                    listOf(16, 74), // p3, p2

                ),
                "t3" to listOf(
                    listOf(30),
                    listOf(31),
                    listOf(32),
                    listOf(33),
                ),
                "t0" to listOf(
                    listOf(40, 41, 42),
                    listOf(43, 44, 45)
                )
            ),
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
            ),
            placeToTypes = mapOf(
                "b2" to 4,
                "o3" to 1,
                "p3" to 2,
                "t2" to 3
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
        val tokenSlice = buildTransitionHistory(model)

        tokenSlice.print()
        assertEquals(9, tokenSlice.tokensAt(model.place("p1")).size)
        assertEquals(14, tokenSlice.tokensAt(model.place("p2")).size)
        assertEquals(9, tokenSlice.tokensAt(model.place("p3")).size)
        assertEquals(2, tokenSlice.tokensAt(model.place("p4")).size)
        assertEquals(4, tokenSlice.tokensAt(model.place("p5")).size)
    }

    fun buildModel(): ModelAccessor {
        return ocnet().toDefaultSim(
            SimulationInput(
                transitions = mapOf(
                    "test" to TransitionSetting(
                        synchronizedArcGroups = listOf(
                            SynchronizedArcGroup(syncTransition = "t1", listOf("p1", "p2")),
                            SynchronizedArcGroup("t2", listOf("p3", "p2")),
                            SynchronizedArcGroup("t0", listOf("p4"))
                        )
                    )
                )
            )
        )
    }

    @Test
    fun modelBuildingTest() {
        val model = buildModel()

        model.transitionBy("test").printIndependentArcGroups()
        assertEquals(2, model.transitionBy("test").independentMultiArcConditions.size)
    }

    @Test
    @Disabled("first iteration of solver is superseded with the second")
    fun testMultiSearchTokenSolver() {
        val model = ocnet().toDefaultSim(
            SimulationInput(
                transitions = mapOf(
                    "test" to TransitionSetting(
                        synchronizedArcGroups = listOf(
                            SynchronizedArcGroup(syncTransition = "t1", listOf("p1", "p2")),
                            SynchronizedArcGroup("t2", listOf("p3", "p2")),
                            SynchronizedArcGroup("t0", listOf("p4"))
                        )
                    )
                )
            )
        )
        val tokenSlice = buildTransitionHistory(model)

        val transitionSynchronizationArcSolver = TransitionSynchronizationArcSolver(model.transitionBy("test"))

        val normalShuffler = NormalShuffler(random = Random(42))

        measureTimeMillis {
            for (i in 0..<1000) {

                val solutions =
                    transitionSynchronizationArcSolver.getSolutionFinderIterable(
                        tokenSlice,
                        normalShuffler,
                        NoTokenGenerator
                    )!!
                        .iterator()
                        .asSequence().toList()
                assertEquals(24, solutions.size)
            }
        }.print()
    }

    @Test
    fun testMultiSearchTokenSolver2() {
        val model = ocnet().toDefaultSim(
            SimulationInput(
                transitions = mapOf(
                    "test" to TransitionSetting(
                        synchronizedArcGroups = listOf(
                            SynchronizedArcGroup(syncTransition = "t1", listOf("p1", "p2")),
                            SynchronizedArcGroup("t2", listOf("p3", "p2")),
                            SynchronizedArcGroup("t0", listOf("p4"))
                        )
                    )
                )
            )
        )
        val tokenSlice = buildTransitionHistory(model)
        val normalShuffler = NormalShuffler(random = Random(42))
        measureTimeMillis {
            for (i in 0..1000) {
                val solutions =
                    TransitionSyncV2FullSolutionFinder(
                        model.transitionBy("test"),
                        shuffler = normalShuffler,
                        NoTokenGenerator
                    )
                        .findSolution(tokenSlice)
                        .asSequence()
                        .toList()

                assertEquals(6, solutions.size)
            }
        }.print()
    }
}