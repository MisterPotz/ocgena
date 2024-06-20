package ru.misterpotz.ocgena.simulation_v2.simulation

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.simulation_v2.StepSequenceLogger
import ru.misterpotz.ocgena.simulation_v2.input.Interval
import ru.misterpotz.ocgena.simulation_v2.input.PlaceSetting
import ru.misterpotz.ocgena.simulation_v2.input.SimulationInput
import ru.misterpotz.ocgena.simulation_v2.input.TransitionSetting
import ru.misterpotz.ocgena.simulation_v2.prettyString
import ru.misterpotz.ocgena.simulation_v2.utils.toSimComp
import ru.misterpotz.ocgena.testing.build3Tran4InpExample
import kotlin.time.Duration

class FullUnsycnEntitiesSimulationTest {
    @Test
    fun printNet() {
        println(build3Tran4InpExample().toDot().let { println(it) })
    }

    @Test
    fun simulation() = runTest(timeout = Duration.INFINITE) {
        val model = build3Tran4InpExample()
        val simulationinput = SimulationInput(
            places = mapOf(
                "input1" to PlaceSetting(initialTokens = 3),
                "input2" to PlaceSetting(initialTokens = 3),
                "input3" to PlaceSetting(initialTokens = 3),
                "input4" to PlaceSetting(initialTokens = 3)
            ),
            transitions = mapOf(
                "t1" to TransitionSetting(
                    eftLft = Interval(50..100)
                ),
            ),
            randomSeed = 42,
            loggingEnabled = true
        )

        val logger = StepSequenceLogger()
        val sim = model.toSimComp(simulationinput, logger = logger)

        sim.simulation().runSimulation()

        println(logger.events)
        println(logger.logs.prettyString())
        Assertions.assertEquals(10, logger.events.size)
    }
}