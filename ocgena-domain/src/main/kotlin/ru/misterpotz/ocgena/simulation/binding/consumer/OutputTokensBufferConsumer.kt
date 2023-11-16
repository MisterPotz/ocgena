package ru.misterpotz.ocgena.simulation.binding.consumer

import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.registries.ArcsMultiplicityRegistry
import ru.misterpotz.ocgena.simulation.binding.buffer.OutputMissingTokensGenerator
import ru.misterpotz.ocgena.simulation.binding.buffer.OutputTokensBufferConsumer
import ru.misterpotz.ocgena.simulation.binding.buffer.TransitionBufferInfo
import ru.misterpotz.ocgena.simulation.binding.generator.OutputMissingTokensGeneratorFactory
import ru.misterpotz.ocgena.simulation.interactors.TokenSelectionInteractor
import javax.inject.Inject

class OutputTokensBufferConsumerImpl(
    private val transitionBufferInfo: TransitionBufferInfo,
    private val transition: Transition,
    private val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
    private val transitionTokenSelectionInteractor: TokenSelectionInteractor,
    private val outputMissingTokensGeneratorFactory: OutputMissingTokensGeneratorFactory
) : OutputTokensBufferConsumer {

    override fun transitionBufferInfo(): TransitionBufferInfo {
        return transitionBufferInfo
    }

    override fun consumeTokenBuffer(): OutputMissingTokensGenerator {
        val outputPlaces = transition.toPlaces
        val outputMarking = PlaceToObjectMarking()

        for (outputPlace in outputPlaces) {
            val outputArcId = outputPlace.arcIdTo(transition.id)

            val arcMultiplicity = arcsMultiplicityRegistry.transitionOutputMultiplicity(
                transitionBufferInfo,
                outputArcId
            )

            val sourceBuffer = arcMultiplicity.getTokenSourceForThisArc()
            val tokensToConsume = arcMultiplicity.requiredTokenAmount()

            val consumedTokens =
                transitionTokenSelectionInteractor.selectAndRemoveTokensFromBuffer(sourceBuffer, tokensToConsume)

            outputMarking[outputPlace].addAll(consumedTokens)
        }

        return outputMissingTokensGeneratorFactory.create(
            transitionBufferInfo,
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
        transitionBufferInfo: TransitionBufferInfo,
        transition: Transition,
    ): OutputTokensBufferConsumer {
        return OutputTokensBufferConsumerImpl(
            transitionBufferInfo = transitionBufferInfo,
            transition = transition,
            arcsMultiplicityRegistry = arcsMultiplicityRegistry,
            transitionTokenSelectionInteractor = transitionTokenSelectionInteractor,
            outputMissingTokensGeneratorFactory = outputMissingTokensGeneratorFactory
        )
    }
}
