package ru.misterpotz.ocgena.simulation_v2.simulation

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.simulation_v2.StepSequenceLogger
import ru.misterpotz.ocgena.simulation_v2.input.*
import ru.misterpotz.ocgena.simulation_v2.utils.toSimComp
import ru.misterpotz.ocgena.testing.buildAdvancedSynchronizingLomazovaExampleModel
import ru.misterpotz.ocgena.testing.buildSynchronizingLomazovaExampleModel
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
//                    SynchronizedArcGroup(syncTransition = "send_invoices", listOf("b2", "o3")),
//                    SynchronizedArcGroup("place_order", listOf("o3", "p3")),
//                    SynchronizedArcGroup("arrange_packages_to_tracks", listOf("p3", "t2"))
                )
            )
        ),
        places = mapOf(
            "bill-source" to PlaceSetting(initialTokens = 400),
            "package-source" to PlaceSetting(initialTokens = 600),
            "order-source" to PlaceSetting(initialTokens = 700),
            "track-source" to PlaceSetting(initialTokens = 800)
        )
    )

    @Test
    fun fullSimulationTestWithTime() = runTest(timeout = Duration.INFINITE) {
        val logger = StepSequenceLogger()

        val sim = ocnet.toSimComp(
            simulationInput,
            logger
        )

        launch {
            sim.simulation().runSimulation()
        }.join()

//        Assertions.assertTrue(logger.events.size > 0)
        println(logger.events)
        println(logger.logs.joinToString("\n") { it.prettyString() })
//        Assertions.assertEquals(6, logger.events.size)

//        println(sim.model().transitionBy("send_invoices").transitionHistory)
//        println(sim.model().transitionBy("arrange_packages_to_tracks").transitionHistory)
//        println(sim.model().transitionBy("place_order").transitionHistory)
        println(sim.tokenstore())
    }

}