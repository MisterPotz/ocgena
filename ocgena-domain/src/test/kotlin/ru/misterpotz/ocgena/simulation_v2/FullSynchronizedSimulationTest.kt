package ru.misterpotz.ocgena.simulation_v2

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.simulation_old.SimulationTaskStepExecutor
import ru.misterpotz.ocgena.simulation_v2.algorithm.simulation.StepExecutor
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.NormalShuffler
import ru.misterpotz.ocgena.simulation_v2.di.SimulationV2Component
import ru.misterpotz.ocgena.simulation_v2.input.PlaceSetting
import ru.misterpotz.ocgena.simulation_v2.input.SimulationInput
import ru.misterpotz.ocgena.simulation_v2.input.SynchronizedArcGroup
import ru.misterpotz.ocgena.simulation_v2.input.TransitionSetting
import ru.misterpotz.ocgena.simulation_v2.utils.toDefaultSim
import ru.misterpotz.ocgena.simulation_v2.utils.toSimComp
import kotlin.math.log
import kotlin.random.Random

class FullSynchronizedSimulationTest {

    @Test
    fun fullSimulationTest() = runTest {
        val ocnet = buildSynchronizingLomazovaExampleModel()

        val logger = StepSequenceLogger()

        val sim = ocnet.toSimComp(
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

        sim.simulation().runSimulation()

        println(logger.events)
        println(logger.logs)
    }
}