package ru.misterpotz.ocgena.simulation_v2.simulation

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.simulation_v2.StepSequenceLogger
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.NormalShuffler
import ru.misterpotz.ocgena.simulation_v2.algos.buildSynchronizingLomazovaExampleModel
import ru.misterpotz.ocgena.simulation_v2.input.*
import ru.misterpotz.ocgena.simulation_v2.utils.toSimComp
import kotlin.random.Random
import kotlin.time.Duration

class FullSynchronizedSimulationTest {

    @Test
    fun testShuffling() {
        val shuffler = NormalShuffler(Random(42))

        println(shuffler.makeShuffled(1..5))
        println(shuffler.makeShuffled(1..5))
        println(shuffler.makeShuffled(1..5))
        println(shuffler.makeShuffled(1..5))
    }

    @Test
    fun fullSimulationTest() = runTest(timeout = Duration.INFINITE) {
        val ocnet = buildSynchronizingLomazovaExampleModel()

        val logger = StepSequenceLogger()

        val sim = ocnet.toSimComp(
            SimulationInput(
                randomSeed = 42,
                loggingEnabled = true,
                transitions = mapOf(
                    "test_all_sync" to TransitionSetting(
                        synchronizedArcGroups = listOf(
                            SynchronizedArcGroup(syncTransition = "send_invoices", listOf("b2", "o3")),
                            SynchronizedArcGroup("place_order", listOf("o3", "p3")),
                            SynchronizedArcGroup("arrange_packages_to_tracks", listOf("p3", "t2"))
                        )
                    )
                ),
                places = mapOf(
                    "bill" to PlaceSetting(initialTokens = 4),
                    "package" to PlaceSetting(initialTokens = 12),
                    "order" to PlaceSetting(initialTokens = 4),
                    "track" to PlaceSetting(initialTokens = 4)
                )
            ),
            logger
        )

        launch {
            sim.simulation().runSimulation()
        }.join()

        Assertions.assertTrue(logger.events.size > 0)
        println(logger.events)
        Assertions.assertEquals(6, logger.events.size)

        println(sim.model().transitionBy("send_invoices").transitionHistory)
        println(sim.model().transitionBy("arrange_packages_to_tracks").transitionHistory)
        println(sim.model().transitionBy("place_order").transitionHistory)
        println(sim.tokenstore())
        val tokenStore = sim.tokenstore()

        Assertions.assertEquals(3, tokenStore.amountAt(sim.model().place("order")))
        Assertions.assertEquals(
            12,
            tokenStore.issuedTokens.size,
            "Token garbage collection does not work / they are not generated properly / or transitions became broken"
        )
        Assertions.assertEquals(
            0,
            tokenStore.tokensAt(sim.model().place("output")).size,
            "output place must be empty regarding tokens - cleaning doesn't work properly"
        )
        Assertions.assertEquals(
            1,
            tokenStore.amountAt(sim.model().place("output")),
            "output token number must be correct"
        )
    }

    @Test
    fun fullSimulationTestWithTime() = runTest(timeout = Duration.INFINITE) {
        val ocnet = buildSynchronizingLomazovaExampleModel()

        val logger = StepSequenceLogger()

        val sim = ocnet.toSimComp(
            SimulationInput(
                randomSeed = 42,
                loggingEnabled = true,
                defaultEftLft = Interval(10..120),
                transitions = mapOf(
                    "test_all_sync" to TransitionSetting(
                        synchronizedArcGroups = listOf(
                            SynchronizedArcGroup(syncTransition = "send_invoices", listOf("b2", "o3")),
                            SynchronizedArcGroup("place_order", listOf("o3", "p3")),
                            SynchronizedArcGroup("arrange_packages_to_tracks", listOf("p3", "t2"))
                        )
                    )
                ),
                places = mapOf(
                    "bill" to PlaceSetting(initialTokens = 4),
                    "package" to PlaceSetting(initialTokens = 12),
                    "order" to PlaceSetting(initialTokens = 4),
                    "track" to PlaceSetting(initialTokens = 4)
                )
            ),
            logger
        )

        launch {
            sim.simulation().runSimulation()
        }.join()

        Assertions.assertTrue(logger.events.size > 0)
        println(logger.events)
        println(logger.logs.joinToString("\n") { it.prettyString() })
        Assertions.assertEquals(6, logger.events.size)

        println(sim.model().transitionBy("send_invoices").transitionHistory)
        println(sim.model().transitionBy("arrange_packages_to_tracks").transitionHistory)
        println(sim.model().transitionBy("place_order").transitionHistory)
        println(sim.tokenstore())
        val tokenStore = sim.tokenstore()

    }
}