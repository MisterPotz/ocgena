package ru.misterpotz.ocgena.simulation_v2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.simulation_v2.algorithm.simulation.PlaceWrapper
import ru.misterpotz.ocgena.simulation_v2.algorithm.simulation.TransitionArcSolver
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.NormalShuffler
import ru.misterpotz.ocgena.simulation_v2.entities.TokenWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.TransitionWrapper
import ru.misterpotz.ocgena.simulation_v2.entities_selection.ModelAccessor
import ru.misterpotz.ocgena.simulation_v2.entities_storage.SimpleTokenSlice
import ru.misterpotz.ocgena.simulation_v2.input.SimulationInput
import ru.misterpotz.ocgena.simulation_v2.input.SynchronizedArcGroup
import ru.misterpotz.ocgena.simulation_v2.input.TransitionSetting
import ru.misterpotz.ocgena.simulation_v2.utils.toDefaultSim
import ru.misterpotz.ocgena.testing.buildOCNet
import kotlin.random.Random


class TransitionArcSolverTest {
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

    private fun buildTransitionHistory(model: ModelAccessor): SimpleTokenSlice {
        fun List<Int>.makeTokens(): List<TokenWrapper> = map { TokenWrapper(it.toString(), model.defaultObjectType()) }


        val t1History = listOf(
            listOf(1, 2, 100), // p1, p3, p2
            listOf(3, 4, 102), // p1, p3, p2
            listOf(5, 6, 103), // p1, p3, p2
            listOf(7, 8, 104), // p1, p3, p2
            listOf(9, 10, 105), // p1, p2, p2
            listOf(71, 72, 106), // p1, p2, p2
            listOf(73, 74, 107), // p1, p2, p2
        )

        val t2History = listOf(
            listOf(2, 11), // p3, p2
            listOf(4, 12), // p3, p2
            listOf(6, 13), // p3, p2
            listOf(8, 14), // p3, p2

            listOf(14, 10), // p3, p2
            listOf(15, 72), // p3, p2
            listOf(16, 74), // p3, p2
        )

        val t3History = listOf(
            listOf(30),
            listOf(31),
            listOf(32),
            listOf(33),
        )

        val t0History = listOf(
            listOf(40, 41, 42),
            listOf(43, 44, 45)
        )
        val tokenEntries = mutableMapOf<String, TokenWrapper>()
        fun List<List<Int>>.recordTokensToHistory(transitionWrapper: TransitionWrapper): List<TokenWrapper> {
            return flatMap { it ->
                val tokens = it.map { tokenIndex ->
                    tokenEntries.getOrPut(tokenIndex.toString()) {
                        TokenWrapper(
                            tokenIndex.toString(),
                            model.defaultObjectType()
                        )
                    }
                }
                val newLogReference = transitionWrapper.getNewTransitionReference()
                for (token in tokens) {
                    transitionWrapper.addTokenVisit(newLogReference, token)
                }
                tokens
            }
        }

        fun List<TokenWrapper>.by(int: Int): TokenWrapper {
            val id = int.toString()
            return find { it.tokenId == id }!!
        }

        val allTokens = listOf(
            t1History to model.transitionBy("t1"),
            t2History to model.transitionBy("t2"),
            t3History to model.transitionBy("t3"),
            t0History to model.transitionBy("t0")
        ).flatMap { (history, transition) -> history.recordTokensToHistory(transition) }

        fun List<Int>.selectTokens(): List<TokenWrapper> {
            return map { allTokens.by(it) }
        }

        val tokenSlice = SimpleTokenSlice.build {
            // from t1
            addTokens(model.place("p1"), listOf(1, 3, 5, 7, 9, 71, 73).selectTokens())
            // from t0
            addTokens(model.place("p1"), listOf(40, 41).selectTokens())

            // from t2
            addTokens(
                model.place("p2"),
                listOf(11, 12, 13, 14, 10, 72, 74, 100, 102, 103, 104, 105, 106, 107).selectTokens()
            )

            // from t2
            addTokens(model.place("p3"), listOf(2, 4, 6, 8, 14, 15, 16).selectTokens())
            // from t0
            addTokens(model.place("p3"), listOf(42, 43).selectTokens())

            // from t0
            addTokens(model.place("p4"), listOf(44, 45).selectTokens())

            // from t3
            addTokens(model.place("p5"), listOf(30, 31, 32, 33).selectTokens())
        }

        return tokenSlice
    }

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

    fun SimpleTokenSlice.copyFromMap(
        model: ModelAccessor,
        map: Map<String, List<Int>>
    ): Map<PlaceWrapper, List<TokenWrapper>> {
        return buildMap {
            for ((place, tokens) in map) {
                put(model.place(place), tokens.map { tokenBy(it.toString()) })
            }
        }
    }

    @Test
    fun testTokenSolver() {
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

        val transitionArcSolver = TransitionArcSolver(model.transitionBy("test"))

        val normalShuffler = NormalShuffler(random = Random(42))

        val solutionMap = mapOf(
            "p1" to listOf(9),
            "p2" to listOf(10),
            "p3" to listOf(14),
            "p4" to listOf(45)
        )
        assertEquals(
            tokenSlice.copyFromMap(model, solutionMap),
            transitionArcSolver.findAnyExistingSolution(tokenSlice, shuffler = normalShuffler)
        )
    }

    @Test
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

        val transitionArcSolver = TransitionArcSolver(model.transitionBy("test"))

        val normalShuffler = NormalShuffler(random = Random(42))

        val solutions = listOf(
            mapOf(
                "p1" to listOf(9),
                "p2" to listOf(10),
                "p3" to listOf(14),
                "p4" to listOf(45)
            ),
            mapOf(
                "p1" to listOf(71),
                "p2" to listOf(72),
                "p3" to listOf(15),
                "p4" to listOf(45)
            ),
            mapOf(
                "p1" to listOf(73),
                "p2" to listOf(74),
                "p3" to listOf(16),
                "p4" to listOf(45)
            ),
            //
            mapOf(
                "p1" to listOf(9),
                "p2" to listOf(10),
                "p3" to listOf(14),
                "p4" to listOf(44)
            ),
            mapOf(
                "p1" to listOf(71),
                "p2" to listOf(72),
                "p3" to listOf(15),
                "p4" to listOf(44)
            ),
            mapOf(
                "p1" to listOf(73),
                "p2" to listOf(74),
                "p3" to listOf(16),
                "p4" to listOf(44)
            )
        )

        assertEquals(
            solutions.map { tokenSlice.copyFromMap(model, it) },
            transitionArcSolver.getSolutionFinderIterable(tokenSlice, normalShuffler)!!.iterator().asSequence().toList()
        )
    }
}