package ru.misterpotz.ocgena.simulation.binding.consumer

import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.registries.ArcsMultiplicityRegistry
import ru.misterpotz.ocgena.simulation.binding.buffer.OutputMissingTokensFiller
import ru.misterpotz.ocgena.simulation.binding.buffer.OutputTokensBufferConsumer
import ru.misterpotz.ocgena.simulation.binding.buffer.TokenGroupedInfo
import ru.misterpotz.ocgena.simulation.binding.generator.OutputMissingTokensGeneratorFactory
import ru.misterpotz.ocgena.simulation.interactors.TokenSelectionInteractor
import javax.inject.Inject

class OutputTokensBufferConsumerImpl(
    private val tokenGroupedInfo: TokenGroupedInfo,
    private val transition: Transition,
    private val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
    private val transitionTokenSelectionInteractor: TokenSelectionInteractor,
    private val outputMissingTokensGeneratorFactory: OutputMissingTokensGeneratorFactory
) : OutputTokensBufferConsumer {

    override fun transitionBufferInfo(): TokenGroupedInfo {
        return tokenGroupedInfo
    }

    override fun consumeTokenBuffer(): OutputMissingTokensFiller {
        val outputPlaces = transition.toPlaces
        val outputMarking = PlaceToObjectMarking()

        for (outputPlace in outputPlaces) {
            val outputArcId = transition.id.arcIdTo(outputPlace)

            val arcMultiplicity = arcsMultiplicityRegistry.transitionOutputMultiplicity(
                tokenGroupedInfo,
                outputArcId
            )

            val sourceBuffer = arcMultiplicity.getTokenSourceForThisArc()
            val tokensToConsume = arcMultiplicity.requiredTokenAmount()

            val consumedTokens =
                sourceBuffer
                    ?.let { transitionTokenSelectionInteractor.selectTokensFromBuffer(it, tokensToConsume) }

            if (consumedTokens != null) {
                sourceBuffer.removeAll(consumedTokens)
                outputMarking[outputPlace].addAll(consumedTokens)
            }
        }

        return outputMissingTokensGeneratorFactory.create(
            tokenGroupedInfo,
            transition,
            outputMarking
        )
    }
}

class OutputTokensBufferConsumerFactory @Inject constructor(
    private val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
    private val transitionTokenSelectionInteractor: TokenSelectionInteractor,
    private val outputMissingTokensGeneratorFactory: OutputMissingTokensGeneratorFactory
) {
    fun create(
        tokenGroupedInfo: TokenGroupedInfo,
        transition: Transition,
    ): OutputTokensBufferConsumer {
        return OutputTokensBufferConsumerImpl(
            tokenGroupedInfo = tokenGroupedInfo,
            transition = transition,
            arcsMultiplicityRegistry = arcsMultiplicityRegistry,
            transitionTokenSelectionInteractor = transitionTokenSelectionInteractor,
            outputMissingTokensGeneratorFactory = outputMissingTokensGeneratorFactory
        )
    }
}
