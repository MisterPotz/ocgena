package ru.misterpotz.ocgena.serialization

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.DEFAULT_SETTINGS
import ru.misterpotz.ocgena.ModelPath
import ru.misterpotz.ocgena.readAndBuildConfig
import ru.misterpotz.ocgena.simulation.config.original.TransitionsOriginalSpec
import ru.misterpotz.ocgena.simulation.config.original.withUntilNext
import kotlin.io.path.Path

class ConfigLoadTest {

    @Test
    fun simConfigLoadsOkay() {

        val simConfig = readAndBuildConfig(
            DEFAULT_SETTINGS,
            ModelPath.ONE_IN_TWO_OUT
        )

        Assertions.assertNotNull(simConfig)
        Assertions.assertEquals(42, simConfig.randomSeed)
    }

    @Test
    fun simConfigUpdatesOk() {
        val simConfig = readAndBuildConfig(
            Path("default_settings.yaml"),
            ModelPath.ONE_IN_TWO_OUT
        )

        Assertions.assertNotNull(simConfig)

        val updatedSimConfig = simConfig.withInitialMarking {
            put("p1", 10)
            put("p2", 30)
        }.withTransitionsTimes<TransitionsOriginalSpec> {
            update {
                put("t1", (15..15).withUntilNext(0..0))
            }
        }.withGenerationPlaces {
            put("p1", 10)
        }

        Assertions.assertEquals(10, updatedSimConfig.initialMarking["p1"])
        Assertions.assertEquals(30, updatedSimConfig.initialMarking["p2"])
        Assertions.assertEquals(
            15..15,
            updatedSimConfig.castTransitions<TransitionsOriginalSpec>()["t1"].duration.intRange
        )
        Assertions.assertEquals(
            0..0,
            updatedSimConfig.castTransitions<TransitionsOriginalSpec>()["t1"]
                .timeUntilNextInstanceIsAllowed
                .intRange
        )
        Assertions.assertEquals(
            10,
            updatedSimConfig.tokenGeneration!!.placeIdToGenerationTarget["p1"]
        )
    }
}
