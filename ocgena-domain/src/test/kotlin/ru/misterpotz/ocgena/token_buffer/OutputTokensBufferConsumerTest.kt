package ru.misterpotz.ocgena.token_buffer

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.*
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArcMeta
import ru.misterpotz.ocgena.simulation_old.config.original.TransitionsOriginalSpec
import ru.misterpotz.ocgena.simulation_old.config.original.withUntilNext
import ru.misterpotz.ocgena.simulation_old.di.SimulationComponent
import ru.misterpotz.ocgena.simulation_old.logging.fastFullDev
import ru.misterpotz.ocgena.testing.*

class OutputTokensBufferConsumerTest {

    fun configureSimComponent(): SimulationComponent {
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
        )
    }

    @Test
    @Disabled
    fun `when aalst and token buffer becomes empty continues with no failure`() = runTest {
        val simComp = configureSimComponent()

        val outputTokensBufferConsumerFactory = simComp.outputTokensBufferConsumerFactory()
        val transition = simComp.transition("t1")

        val transitionBufferInfo = simComp.mockTransitionBufferInfo(
            "t1"
        ) {
            add("o1".withArcMeta(NormalArcMeta(1)).withTokenBuffer(sortedSetOf(1)))
            add("o2".withArcMeta(NormalArcMeta(1)).withTokenBuffer(sortedSetOf()))
        }

        val outputTokensBufferConsumer = outputTokensBufferConsumerFactory.create(
            tokenGroupedInfo = transitionBufferInfo,
            transition = transition
        )

        val outputMissingTokensGenerator = outputTokensBufferConsumer.consumeTokenBuffer()

        Assertions.assertEquals(
            1,
            outputMissingTokensGenerator.currentPlaceToObjectMarking["p2"].size
        )
        Assertions.assertEquals(
            0,
            outputMissingTokensGenerator.currentPlaceToObjectMarking["p3"].size
        )
    }
}