package ru.misterpotz.ocgena.simulation.binding.consumer

import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.registries.ArcsMultiplicityRegistry
import ru.misterpotz.ocgena.simulation.binding.buffer.OutputMissingTokensFiller
import ru.misterpotz.ocgena.simulation.binding.buffer.OutputTokensBufferConsumer
import ru.misterpotz.ocgena.simulation.binding.buffer.TransitionGroupedTokenInfo
import ru.misterpotz.ocgena.simulation.binding.generator.OutputMissingTokensGeneratorFactory
import ru.misterpotz.ocgena.simulation.interactors.TokenSelectionInteractor
import javax.inject.Inject

class OutputTokensBufferConsumerImpl(
    private val transitionGroupedTokenInfo: TransitionGroupedTokenInfo,
    private val transition: Transition,
    private val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
    private val transitionTokenSelectionInteractor: TokenSelectionInteractor,
    private val outputMissingTokensGeneratorFactory: OutputMissingTokensGeneratorFactory
) : OutputTokensBufferConsumer {

    override fun transitionBufferInfo(): TransitionGroupedTokenInfo {
        return transitionGroupedTokenInfo
    }

    override fun consumeTokenBuffer(): OutputMissingTokensFiller {
        val outputPlaces = transition.toPlaces
        val outputMarking = PlaceToObjectMarking()

        for (outputPlace in outputPlaces) {
            val outputArcId = transition.id.arcIdTo(outputPlace)

            val arcMultiplicity = arcsMultiplicityRegistry.transitionOutputMultiplicity(
                transitionGroupedTokenInfo,
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
            transitionGroupedTokenInfo,
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
        transitionGroupedTokenInfo: TransitionGroupedTokenInfo,
        transition: Transition,
    ): OutputTokensBufferConsumer {
        return OutputTokensBufferConsumerImpl(
            transitionGroupedTokenInfo = transitionGroupedTokenInfo,
            transition = transition,
            arcsMultiplicityRegistry = arcsMultiplicityRegistry,
            transitionTokenSelectionInteractor = transitionTokenSelectionInteractor,
            outputMissingTokensGeneratorFactory = outputMissingTokensGeneratorFactory
        )
    }
}
