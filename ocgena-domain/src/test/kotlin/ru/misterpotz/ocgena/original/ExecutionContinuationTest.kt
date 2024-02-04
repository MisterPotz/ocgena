package ru.misterpotz.ocgena.original

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.*
import ru.misterpotz.ocgena.simulation.config.original.TransitionsOriginalSpec
import ru.misterpotz.ocgena.simulation.config.original.withUntilNext
import ru.misterpotz.ocgena.simulation.logging.fastFullDev
import ru.misterpotz.ocgena.testing.FacadeSim
import ru.misterpotz.ocgena.testing.facade
import ru.misterpotz.ocgena.testing.simComponentOld

annotation class TestFolder(val folderName: String)

class ExecutionContinuationTest {

    fun createSimTask(): FacadeSim {
        val simConfig = readAndBuildConfig(
            DEFAULT_SETTINGS,
            ModelPath.ONE_IN_TWO_OUT
        )

        Assertions.assertNotNull(simConfig)

        val updatedSimConfig = simConfig.withInitialMarking {
            put("p1", 5)
        }.withTransitionsTimes<TransitionsOriginalSpec> {
            update {
                put("t1", (15..15).withUntilNext(0..0))
            }
        }.withGenerationPlaces {
            put("p1", 5)
        }

        return simComponentOld(
            updatedSimConfig,
            developmentDebugConfig = fastFullDev()
        ).facade()
    }

    @Test
    fun givenConfigRunsOk() = runTest {
        val (component, task, config) = createSimTask()
        task.prepareAndRunAll()
    }
}



