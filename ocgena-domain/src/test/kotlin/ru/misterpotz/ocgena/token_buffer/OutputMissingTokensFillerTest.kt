package ru.misterpotz.ocgena.token_buffer

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.*
import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArcMeta
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.config.original.TransitionsOriginalSpec
import ru.misterpotz.ocgena.simulation.config.original.withUntilNext
import ru.misterpotz.ocgena.simulation.di.SimulationComponent
import ru.misterpotz.ocgena.simulation.logging.fastFullDev
import ru.misterpotz.ocgena.testing.*
import java.util.SortedSet

class OutputMissingTokensFillerTest {
    private fun configureSimComponent(): SimulationComponent {
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
    fun `when token buffer becomes empty, generates unique tokens at outputs`() {
        val simComp = configureSimComponent()
        simComp.withGenerateTokens {
            addAll(generateTokensOfType("o1", 1, 2))
        }

        val outputMissingTokensGeneratorFactory = simComp.outputMissingTokensGeneratorFactory()
        val transition = simComp.transition("t1")

        val transitionBufferInfo = simComp.mockTransitionBufferInfo(
            "t1"
        ) {
            add("o1".withArcMeta(NormalArcMeta(1)).withTokenBuffer(sortedSetOf()))
            add("o2".withArcMeta(NormalArcMeta(1)).withTokenBuffer(sortedSetOf()))
        }

        val outputMissingTokensFiller = outputMissingTokensGeneratorFactory.create(
            tokenGroupedInfo = transitionBufferInfo,
            transition = transition,
            outputMarking = PlaceToObjectMarking(
                buildMap<PetriAtomId, SortedSet<ObjectTokenId>> {
                    put("p2", sortedSetOf(1, 2))
                }.toMutableMap()
            )
        )

        val outputTokens = outputMissingTokensFiller.generateMissingTokens()

        val allSets = outputTokens.keys.map { outputTokens[it] }

        var foundRepetition = false

        allSets.fold(
            mutableSetOf<ObjectTokenId>()
        ) { acc, right ->
            foundRepetition = acc.intersect(right).isNotEmpty()
            acc.addAll(right)
            acc
        }
        println(outputTokens.loggableString(simComp))
        Assertions.assertFalse(foundRepetition)
    }
}