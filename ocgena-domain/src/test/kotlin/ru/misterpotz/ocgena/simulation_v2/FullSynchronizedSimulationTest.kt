package ru.misterpotz.ocgena.simulation_v2

import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.simulation.SimulationTaskStepExecutor
import ru.misterpotz.ocgena.simulation_v2.algorithm.simulation.StepExecutor
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.NormalShuffler
import ru.misterpotz.ocgena.simulation_v2.input.SimulationInput
import ru.misterpotz.ocgena.simulation_v2.input.SynchronizedArcGroup
import ru.misterpotz.ocgena.simulation_v2.input.TransitionSetting
import ru.misterpotz.ocgena.simulation_v2.utils.toDefaultSim
import kotlin.random.Random

class FullSynchronizedSimulationTest {

    @Test
    fun fullSimulationTest() {
        val ocnet = buildSynchronizingLomazovaExampleModel()
        val model = ocnet.toDefaultSim(
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
        val shuffler = NormalShuffler(Random(42))



//        SimulationTaskStepExecutor(StepExecutor())

    }
}