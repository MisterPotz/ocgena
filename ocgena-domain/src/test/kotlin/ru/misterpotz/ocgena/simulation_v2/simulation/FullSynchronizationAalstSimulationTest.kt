package ru.misterpotz.ocgena.simulation_v2.simulation

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.simulation_v2.StepSequenceLogger
import ru.misterpotz.ocgena.simulation_v2.algos.buildAalstArcModel
import ru.misterpotz.ocgena.simulation_v2.input.*
import ru.misterpotz.ocgena.simulation_v2.prettyString
import ru.misterpotz.ocgena.simulation_v2.utils.toSimComp
import kotlin.time.Duration

class FullSynchronizationAalstSimulationTest {


    @Test
    fun printModel() {
        buildAalstArcModel().toDot().let { println(it) }
    }

    @Test
    fun fullSimulationTest() = runTest(timeout = Duration.INFINITE) {
        val ocnet = buildAalstArcModel()

        val logger = StepSequenceLogger()

        val sim = ocnet.toSimComp(
            SimulationInput(
                randomSeed = 42,
                loggingEnabled = false,
                transitions = mapOf(),
                places = mapOf(
                    "input" to PlaceSetting(initialTokens = 10),
                    "input2" to PlaceSetting(initialTokens = 10),
                )
            ),
            logger
        )

        launch {
            sim.simulation().runSimulation()
        }.join()

        println(logger.events)
        println(logger.logs.prettyString())

        Assertions.assertTrue(logger.events.size > 0)

        Assertions.assertEquals(
            11,
            logger.logs.size,
            "can depend on random key and the sequence in which randomizer functions invoked\n" +
                    "         can fail if algorithm changes"
        )
    }

    @Test
    fun fullSimulationTestWithTime() = runTest(timeout = Duration.INFINITE) {
        val ocnet = buildAalstArcModel()

        val logger = StepSequenceLogger()

        val sim = ocnet.toSimComp(
            SimulationInput(
                randomSeed = 42,
                loggingEnabled = false,
                defaultEftLft = Interval(10..120),
                transitions = mapOf(),
                places = mapOf(
                    "input" to PlaceSetting(initialTokens = 10),
                    "input2" to PlaceSetting(initialTokens = 10),
                )
            ),
            logger
        )

        launch {
            sim.simulation().runSimulation()
        }.join()

        println(logger.events)
        println(logger.logs.prettyString())

        Assertions.assertTrue(logger.events.size > 0)

        Assertions.assertEquals(
            11,
            logger.logs.size,
            "can depend on random key and the sequence in which randomizer functions invoked\n" +
                    "         can fail if algorithm changes"
        )
    }
}