package ru.misterpotz.ocgena.simulation.binding.generator

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.registries.ArcsMultiplicityRegistry
import ru.misterpotz.ocgena.simulation.binding.buffer.OutputMissingTokensFiller
import ru.misterpotz.ocgena.simulation.binding.buffer.TransitionGroupedTokenInfo
import ru.misterpotz.ocgena.simulation.interactors.TokenSelectionInteractor
import javax.inject.Inject

class OutputMissingTokensFillerImpl(
    private val transitionGroupedTokenInfo: TransitionGroupedTokenInfo,
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
                transitionGroupedTokenInfo,
                outputArcId
            )

            val existingTokens = currentPlaceToObjectMarking[outputPlace]
            val needToGenerateAdditionalTokens =
                existingTokens.size < arcMultiplicity.requiredTokenAmount()

            if (needToGenerateAdditionalTokens) {
                val tokensToGenerate = arcMultiplicity.requiredTokenAmount() - existingTokens.size

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
        transitionGroupedTokenInfo: TransitionGroupedTokenInfo,
        transition: Transition,
        outputMarking: PlaceToObjectMarking
    ): OutputMissingTokensFiller {
        return OutputMissingTokensFillerImpl(
            transitionGroupedTokenInfo,
            transition,
            ocNet,
            arcsMultiplicityRegistry,
            transitionTokenSelectionInteractor,
            outputMarking
        )
    }
}