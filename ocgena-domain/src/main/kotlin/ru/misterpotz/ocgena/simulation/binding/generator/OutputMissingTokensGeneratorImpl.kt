package ru.misterpotz.ocgena.simulation.binding.generator

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.registries.ArcsMultiplicityRegistry
import ru.misterpotz.ocgena.simulation.binding.buffer.OutputMissingTokensGenerator
import ru.misterpotz.ocgena.simulation.binding.buffer.TransitionBufferInfo
import ru.misterpotz.ocgena.simulation.interactors.TokenSelectionInteractor
import javax.inject.Inject

class OutputMissingTokensGeneratorImpl(
    private val transitionBufferInfo: TransitionBufferInfo,
    private val transition: Transition,
    private val ocNet: OCNet,
    private val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
    private val transitionTokenSelectionInteractor: TokenSelectionInteractor,
    private val outputMarking: PlaceToObjectMarking
) : OutputMissingTokensGenerator {
    override fun generateMissingTokens(): ImmutablePlaceToObjectMarking {
        val outputPlaces = transition.toPlaces

        for (outputPlace in outputPlaces) {
            val outputArcId = outputPlace.arcIdTo(transition.id)

            val arcMultiplicity = arcsMultiplicityRegistry.transitionOutputMultiplicity(
                transitionBufferInfo,
                outputArcId
            )

            val needToGenerateTokens = !arcMultiplicity.sourceBufferHasEnoughTokens()

            if (needToGenerateTokens) {
                val tokensToConsume = arcMultiplicity.requiredTokenAmount()

                val outputPlaceType = ocNet.placeToObjectTypeRegistry[outputPlace]

                val consumedTokens =
                    transitionTokenSelectionInteractor.generateTokens(outputPlaceType, tokensToConsume)

                outputMarking[outputPlace].addAll(consumedTokens)
            }
        }
        return outputMarking.toImmutable()
    }
}

class OutputMissingTokensGeneratorFactory @Inject constructor(
    private val ocNet: OCNet,
    private val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
    private val transitionTokenSelectionInteractor: TokenSelectionInteractor,
) {
    fun create(
        transitionBufferInfo: TransitionBufferInfo,
        transition: Transition,
        outputMarking: PlaceToObjectMarking
    ): OutputMissingTokensGenerator {
        return OutputMissingTokensGeneratorImpl(
            transitionBufferInfo,
            transition,
            ocNet,
            arcsMultiplicityRegistry,
            transitionTokenSelectionInteractor,
            outputMarking
        )
    }
}