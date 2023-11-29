package ru.misterpotz.ocgena.dsl.simulation

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.dsl.DEFAULT_SETTINGS
import ru.misterpotz.ocgena.dsl.ModelPath
import ru.misterpotz.ocgena.dsl.readAndBuildConfig
import ru.misterpotz.ocgena.dsl.simComponent
import ru.misterpotz.ocgena.simulation.config.withUntilNext
import ru.misterpotz.ocgena.simulation.logging.fastFullDev

annotation class TestFolder(val folderName: String)

class ExecutionContinuationTest {

    @Test
    fun stopsWhenMarkingHasFiveTokens() = runTest {
        val simConfig = readAndBuildConfig(
            DEFAULT_SETTINGS,
            ModelPath.ONE_IN_TWO_OUT
        )

        Assertions.assertNotNull(simConfig)

        val updatedSimConfig = simConfig.withInitialMarking {
            put("p1", 5)
        }.withTransitionsTimes {
            put("t1", (15..15).withUntilNext(0..0))
        }.withGenerationPlaces {
            put("p1", 5)
        }

        val simTask = simComponent(
            updatedSimConfig,
            developmentDebugConfig = fastFullDev()
        )
            .simulationTask()
        simTask.prepareAndRunAll()

        Assertions.assertNotNull(simConfig)
    }
}



