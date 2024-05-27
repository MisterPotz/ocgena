package ru.misterpotz.ocgena.simulation_v2.simulation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.simulation_v2.SizeLogger
import ru.misterpotz.ocgena.simulation_v2.input.*
import ru.misterpotz.ocgena.simulation_v2.utils.toSimComp
import ru.misterpotz.ocgena.testing.buildAdvancedSynchronizingLomazovaExampleModel
import kotlin.time.Duration

class FullHugeSynchronizedSimulationTest {
    val ocnet = buildAdvancedSynchronizingLomazovaExampleModel()
    val simulationInput = SimulationInput(
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
            "bill-source" to PlaceSetting(initialTokens =       30),
            "package-source" to PlaceSetting(initialTokens =    30),
            "order-source" to PlaceSetting(initialTokens =      30),
            "track-source" to PlaceSetting(initialTokens =      30)
        )
    )

    @Test
    fun fullSimulationTestWithTime() = runTest(timeout = Duration.INFINITE) {
        val logger = SizeLogger()

        val sim = ocnet.toSimComp(
            simulationInput,
            logger
        )

        launch {
            withContext(Dispatchers.Default) {
                flow<Unit> {
                    println("started")
                    sim.simulation().runSimulation()
                }.catch {
                    println("bad error $it")
                    it.printStackTrace()
                }.collect()
            }
        }.join()

//        Assertions.assertTrue(logger.events.size > 0)
//        println(logger.logs.joinToString("\n") { it.prettyString() })
        println(logger.events)
        println(logger.events.size)
//        println(logger.logs.size)
//        Assertions.assertEquals(6, logger.events.size)

        println(sim.model().transitionBy("send_invoices").transitionHistory)
        println(sim.model().transitionBy("arrange_packages_to_tracks").transitionHistory)
        println(sim.model().transitionBy("place_order").transitionHistory)
        println(sim.model().transitionBy("os-skip").transitionHistory)
        println(sim.model().transitionBy("place_order").transitionHistory)
//        println(sim.tokenstore())
    }

}