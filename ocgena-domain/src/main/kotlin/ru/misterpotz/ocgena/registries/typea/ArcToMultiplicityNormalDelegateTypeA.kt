package ru.misterpotz.ocgena.registries.typea

import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.InputArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.InputArcMultiplicityDynamic
import ru.misterpotz.ocgena.ocnet.primitives.InputArcMultiplicityValue
import ru.misterpotz.ocgena.ocnet.primitives.OutputArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.OutputArcMultiplicityValue
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.registries.ArcsMultiplicityDelegate
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.simulation.binding.buffer.TransitionBufferInfo
import ru.misterpotz.ocgena.simulation.interactors.TokenAmountStorage
import javax.inject.Inject

class ArcToMultiplicityNormalDelegateTypeA @Inject constructor(
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
    ocNet: OCNet,
) : ArcsMultiplicityDelegate() {
    private val inputArcMultiplicityCache : MutableMap<PetriAtomId, InputArcMultiplicityDynamic> = mutableMapOf()
    private val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry = ocNet.placeToObjectTypeRegistry

    override fun transitionInputMultiplicity(arc: Arc): InputArcMultiplicity {
        require(arc is NormalArc)

        val requiredTokens = arc.multiplicity
        val inputNodeId = arc.tailNodeId!!

        val tokensAtPlace = objectTokenRealAmountRegistry.getRealAmountAt(inputNodeId)
        val inputPlaceHasEnoughTokens = tokensAtPlace >= requiredTokens

        return InputArcMultiplicityValue(
            inputPlaceHasEnoughTokens,
            arc.multiplicity
        )
    }

    override fun transitionInputMultiplicityDynamic(arc: Arc): InputArcMultiplicityDynamic {
        require(arc is NormalArc)
        val requiredTokens = arc.multiplicity
        val inputNodeId = arc.tailNodeId!!

        return inputArcMultiplicityCache.getOrPut(arc.id) {
            object : InputArcMultiplicityDynamic {
                override fun inputPlaceHasEnoughTokens(tokenAmountStorage: TokenAmountStorage): Boolean {
                    return tokenAmountStorage.getTokensAt(inputNodeId) >= requiredTokens
                }

                override fun requiredTokenAmount(tokenAmountStorage: TokenAmountStorage): Int {
                    return requiredTokens
                }
            }
        }
    }


    override fun transitionOutputMultiplicity(
        transitionBufferInfo: TransitionBufferInfo,
        arc: Arc
    ): OutputArcMultiplicity {
        require(arc is NormalArc)

        val place = arc.arrowNodeId!!
        val objectType = placeToObjectTypeRegistry[place]

        val tokenBuffer = transitionBufferInfo.getBatchBy(objectType, outputArcMeta = arc.arcMeta)

        return OutputArcMultiplicityValue(
            arc.multiplicity,
            tokenBuffer = tokenBuffer
        )
    }
}
