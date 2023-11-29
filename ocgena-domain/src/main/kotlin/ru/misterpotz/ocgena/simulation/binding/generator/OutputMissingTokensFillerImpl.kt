package ru.misterpotz.ocgena.simulation.binding.generator

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.registries.ArcsMultiplicityRegistry
import ru.misterpotz.ocgena.simulation.binding.buffer.OutputMissingTokensFiller
import ru.misterpotz.ocgena.simulation.binding.buffer.TransitionBufferInfo
import ru.misterpotz.ocgena.simulation.interactors.TokenSelectionInteractor
import javax.inject.Inject

class OutputMissingTokensFillerImpl(
    private val transitionBufferInfo: TransitionBufferInfo,
    private val transition: Transition,
    private val ocNet: OCNet,
    private val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
    private val transitionTokenSelectionInteractor: TokenSelectionInteractor,
    override val currentPlaceToObjectMarking: PlaceToObjectMarking
) : OutputMissingTokensFiller {
    override fun generateMissingTokens(): ImmutablePlaceToObjectMarking {
        val outputPlaces = transition.toPlaces

        for (outputPlace in outputPlaces) {
            val outputArcId = transition.id.arcIdTo(outputPlace)

            val arcMultiplicity = arcsMultiplicityRegistry.transitionOutputMultiplicity(
                transitionBufferInfo,
                outputArcId
            )

            val needToGenerateTokens =

            if (needToGenerateTokens) {
                val tokensToGenerate = arcMultiplicity.requiredTokenAmount() -

                val outputPlaceType = ocNet.placeToObjectTypeRegistry[outputPlace]

                val consumedTokens =
                    transitionTokenSelectionInteractor.generateTokens(outputPlaceType, tokensToGenerate)

                currentPlaceToObjectMarking[outputPlace].addAll(consumedTokens)
            }
        }
        return currentPlaceToObjectMarking.toImmutable()
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
    ): OutputMissingTokensFiller {
        return OutputMissingTokensFillerImpl(
            transitionBufferInfo,
            transition,
            ocNet,
            arcsMultiplicityRegistry,
            transitionTokenSelectionInteractor,
            outputMarking
        )
    }
}