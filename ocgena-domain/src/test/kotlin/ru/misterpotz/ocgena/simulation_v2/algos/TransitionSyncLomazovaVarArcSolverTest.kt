package ru.misterpotz.ocgena.simulation_v2.algos

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.SerializationMode
import ru.misterpotz.ocgena.simulation_v2.NoTokenGenerator
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.NormalShuffler
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.TransitionSynchronizationArcSolver
import ru.misterpotz.ocgena.simulation_v2.buildTransitionHistory
import ru.misterpotz.ocgena.simulation_v2.entities_selection.ModelAccessor
import ru.misterpotz.ocgena.simulation_v2.input.SimulationInput
import ru.misterpotz.ocgena.simulation_v2.input.SynchronizedArcGroup
import ru.misterpotz.ocgena.simulation_v2.input.TransitionSetting
import ru.misterpotz.ocgena.simulation_v2.toTokenSliceFrom
import ru.misterpotz.ocgena.simulation_v2.utils.toDefaultSim
import ru.misterpotz.ocgena.testing.buildSynchronizingLomazovaExampleModel
import ru.misterpotz.ocgena.writeOrAssertYaml
import kotlin.io.path.Path
import kotlin.random.Random


class TransitionSyncLomazovaVarArcSolverTest {
    private fun ocnet() = buildSynchronizingLomazovaExampleModel()

    private fun ModelAccessor.buildTransitionHistoryNonWorking() = buildTransitionHistory(
        this,
        transitionToHistoryEntries = mapOf(
            "place_order" to listOf(
                listOf(1, 10, 11), // order | packages
                listOf(2, 12),      // order | package
                listOf(3, 13, 14, 15), // order | packages
            ),
            // packages of different orders can be on different tracks
            "arrange_packages_to_tracks" to listOf(
                listOf(10, 13, 21),
                listOf(11, 12, 22), // packages | track
                listOf(14, 15, 23), // p3, p2
            ),
            "send_invoices" to listOf(
                listOf(1, 31, 32), // order to bills
                listOf(2, 33, 34),
                listOf(3, 35),
            )
        ),
        placeToTokens = mapOf(
            "b2" to listOf(
                31, 32,
                33, 34,
                35,

                ),
            "o3" to listOf(
                1, 2, 3
            ),
            "p3" to listOf(
                10, 11,
                12,
                13, 14, 15
            ),
            "t2" to listOf(
                21, 22, 23
            )
        ),
 mapOf(
     "b2" to 4,
     "o3" to 1,
     "p3" to 2,
     "t2" to 3
 )
    )

    fun ModelAccessor.buildTransitionHistoryWorking() = buildTransitionHistory(
        this,
        transitionToHistoryEntries = mapOf(
            "place_order" to listOf(
                listOf(1, 10, 11), // order | packages
                listOf(2, 12, 16, 17), // order | package  // <----------------------- DIFF
                listOf(3, 13, 14, 15), // order | packages
            ),
            // packages of different orders can be on different tracks
            "arrange_packages_to_tracks" to listOf(
                listOf(10, 13, 21),
                listOf(11, 12, 22), // packages | track
                listOf(14, 15, 23), // p3, p2
                listOf(16, 17, 24) // <----------------------- DIFF
            ),
            "send_invoices" to listOf(
                listOf(1, 31, 32), // order to bills
                listOf(2, 33, 34),
                listOf(3, 35),
            )
        ),
        placeToTokens = mapOf(
            "b2" to listOf(
                31, 32,
                33, 34,
                35,

                ),
            "o3" to listOf(
                1, 2, 3
            ),
            "p3" to listOf(
                10, 11,
                12,
                13, 14, 15, 16, 17 // <----------------------- DIFF
            ),
            "t2" to listOf(
                21, 22, 23, 24 // <------ DIFF
            )
        ),
        placeToTypes = mapOf(
            "b2" to 4,
            "o3" to 1,
            "p3" to 2,
            "t2" to 3
        )
    )

    @Test
    fun serializationTest() {

        val model = buildSynchronizingLomazovaExampleModel()
        writeOrAssertYaml(model, path = Path("ocnet_lomazova_1.yaml"), SerializationMode.WRITE)
    }

    @Test
    fun print() {
        // http://magjac.com/graphviz-visual-editor/
        println(ocnet().toDot())
    }


    @Test
    fun transitionHistoryBuildTest() {
        val model = ocnet().toDefaultSim()
        val tokenSlice = model.buildTransitionHistoryNonWorking()

        tokenSlice.print()
        assertEquals(5, tokenSlice.tokensAt(model.place("b2")).size)
        assertEquals(3, tokenSlice.tokensAt(model.place("o3")).size)
        assertEquals(6, tokenSlice.tokensAt(model.place("p3")).size)
        assertEquals(3, tokenSlice.tokensAt(model.place("t2")).size)
    }

    fun buildModel(): ModelAccessor {
        return ocnet().toDefaultSim(
            SimulationInput()
        )
    }

    @Test
    fun modelBuildingTest() {
        val model = buildModel()

        model.transitionBy("test_all_sync").printIndependentArcGroups()
        assertEquals(0, model.transitionBy("test_all_sync").independentMultiArcConditions.size)
        assertEquals(4, model.transitionBy("test_all_sync").lomazovaVariabilityArcs.size)
    }

    @Test
    fun testNoSolutionsWhenUnsynchronized() {
        val model = ocnet().toDefaultSim(SimulationInput())
        val tokenSlice = model.buildTransitionHistoryNonWorking()

        val transitionSynchronizationArcSolver = TransitionSynchronizationArcSolver(model.transitionBy("test_all_sync"))
        val normalShuffler = NormalShuffler(random = Random(42))
        val solutions =
            transitionSynchronizationArcSolver.getSolutionFinderIterable(tokenSlice, normalShuffler, NoTokenGenerator)
                ?.iterator()

        println(solutions)

        assertEquals(null, solutions)
    }

    @Test
    fun `no solution due to required synchronization amount and non intersecting history`() {
        val model = ocnet().toDefaultSim(
            SimulationInput(
                loggingEnabled = true,
                transitions = mapOf(
                    "test_all_sync" to TransitionSetting(
                        synchronizedArcGroups = listOf(
                            SynchronizedArcGroup(syncTransition = "send_invoices", listOf("b2", "o3")),
                            SynchronizedArcGroup("place_order", listOf("o3", "p3")),
                            SynchronizedArcGroup("arrange_packages_to_tracks", listOf("p3", "t2"))
                        )
                    )
                )
            )
        )

        assertEquals(1, model.transitionBy("test_all_sync").independentMultiArcConditions.size)
        val tokenSlice = model.buildTransitionHistoryNonWorking()

        val transitionSynchronizationArcSolver = TransitionSynchronizationArcSolver(model.transitionBy("test_all_sync"))
        val normalShuffler = NormalShuffler(random = Random(42))
        val solutions =
            transitionSynchronizationArcSolver.getSolutionFinderIterable(tokenSlice, normalShuffler, NoTokenGenerator)!!
                .iterator()
                .asSequence().toList()

        println(solutions)
        assertEquals(0, solutions.size)
    }

    @Test
    fun `only one solution exists`() {
        val model = ocnet().toDefaultSim(
            SimulationInput(
                loggingEnabled = true,
                transitions = mapOf(
                    "test_all_sync" to TransitionSetting(
                        synchronizedArcGroups = listOf(
                            SynchronizedArcGroup(syncTransition = "send_invoices", listOf("b2", "o3")),
                            SynchronizedArcGroup("place_order", listOf("o3", "p3")),
                            SynchronizedArcGroup("arrange_packages_to_tracks", listOf("p3", "t2"))
                        )
                    )
                )
            )
        )

        val tokenSlice = model.buildTransitionHistoryWorking()
        val normalShuffler = NormalShuffler(random = Random(42))
        val solutions =
            model.transitionBy("test_all_sync").inputArcsSolutions(tokenSlice, normalShuffler, NoTokenGenerator)
                .iterator().asSequence()
                .toList()

        println(solutions)
        assertEquals(1, solutions.size)
        assertEquals(
            mapOf(
                "b2" to listOf(33, 34),
                "o3" to listOf(2),
                "p3" to listOf(16, 17),
                "t2" to listOf(24)
            ).toTokenSliceFrom(tokenSlice, model),
            solutions.first().toTokenSlice()
        )
    }

    @Test
    fun `v2 token solution works`() {
        val model = ocnet().toDefaultSim(
            SimulationInput(
                loggingEnabled = true,
                transitions = mapOf(
                    "test_all_sync" to TransitionSetting(
                        synchronizedArcGroups = listOf(
                            SynchronizedArcGroup(syncTransition = "send_invoices", listOf("b2", "o3")),
                            SynchronizedArcGroup("place_order", listOf("o3", "p3")),
                            SynchronizedArcGroup("arrange_packages_to_tracks", listOf("p3", "t2"))
                        )
                    )
                )
            )
        )

        val tokenSlice = model.buildTransitionHistoryWorking()
        val normalShuffler = NormalShuffler(random = Random(42))
        val solutions =
            model.transitionBy("test_all_sync").inputArcsSolutions(
                tokenSlice,
                normalShuffler,
                NoTokenGenerator,
                v2Solver = true
            )
                .iterator()
                .asSequence()
                .toList()

        assertEquals(
            mapOf(
                "b2" to listOf(33, 34),
                "o3" to listOf(2),
                "p3" to listOf(16, 17),
                "t2" to listOf(24)
            ).toTokenSliceFrom(tokenSlice, model),
            solutions.first().toTokenSlice()
        )
        println(solutions)
    }
}