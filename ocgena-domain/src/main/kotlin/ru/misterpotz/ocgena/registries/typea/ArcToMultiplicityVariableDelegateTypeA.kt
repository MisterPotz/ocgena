package ru.misterpotz.ocgena.registries.typea

import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.*
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArc
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArcMetaTypeA
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.registries.ArcsMultiplicityDelegate
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.simulation.binding.TokenSet
import ru.misterpotz.ocgena.simulation.binding.buffer.TokenGroupedInfo
import ru.misterpotz.ocgena.simulation.interactors.TokenAmountStorage
import javax.inject.Inject

class ArcToMultiplicityVariableDelegateTypeA @Inject constructor(
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
    ocNet: OCNet,
) : ArcsMultiplicityDelegate() {
    private val inputArcMultiplicityCache : MutableMap<PetriAtomId, InputArcMultiplicityDynamic> = mutableMapOf()
    private val outputArcMultiplicityCache : MutableMap<PetriAtomId, OutputArcMultiplicityDynamic> = mutableMapOf()
    private val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry = ocNet.placeToObjectTypeRegistry

    override fun transitionInputMultiplicity(arc: Arc): InputArcMultiplicity {
        require(arc is VariableArc)

        val realTokenAmount = objectTokenRealAmountRegistry.getRealAmountAt(arc.tailNodeId!!)

        return InputArcMultiplicityValue(
            sourceNodeHasEnoughTokens = (realTokenAmount) >= 1,
            requiredTokenAmount = realTokenAmount
        )
    }

    override fun transitionInputMultiplicityDynamic(arc: Arc): InputArcMultiplicityDynamic {
        require(arc is VariableArc)

        return inputArcMultiplicityCache.getOrPut(arc.id) {
            object : InputArcMultiplicityDynamic {
                override fun inputPlaceHasEnoughTokens(tokenAmountStorage: TokenAmountStorage): Boolean {
                    return tokenAmountStorage.getTokensAt(arc.tailNodeId!!) >= 1
                }

                override fun requiredTokenAmount(tokenAmountStorage: TokenAmountStorage): Int {
                    return tokenAmountStorage.getTokensAt(arc.tailNodeId!!)
                }
            }
        }
    }

    override fun transitionOutputMultiplicity(
        tokenGroupedInfo: TokenGroupedInfo,
        arc: Arc
    ): OutputArcMultiplicity {
        require(arc is VariableArc)

        val targetPlace = arc.arrowNodeId!!
        val objectTypeId = placeToObjectTypeRegistry[targetPlace]
        val batchForType = tokenGroupedInfo.getTokenSetBy(
            toPlaceObjectTypeId = objectTypeId,
            outputArcMeta = VariableArcMetaTypeA
        )!!
        val tokensAtBuffer = batchForType.size

        return OutputArcMultiplicityValue(
            requiredTokenAmount = tokensAtBuffer,
            tokenSet = batchForType,
        )
    }

    override fun transitionOutputMultiplicityDynamic(arc: Arc): OutputArcMultiplicityDynamic {
        require(arc is VariableArc)

        return outputArcMultiplicityCache.getOrPut(arc.id) {
            object : OutputArcMultiplicityDynamic {
                override fun requiredTokenAmount(tokenGroupedInfo: TokenGroupedInfo): Int {
                    return getTokenSourceForThisArc(tokenGroupedInfo)!!.size
                }

                override fun getTokenSourceForThisArc(tokenGroupedInfo: TokenGroupedInfo): TokenSet? {
                    val place = arc.arrowNodeId!!
                    val objectType = placeToObjectTypeRegistry[place]

                    return tokenGroupedInfo.getTokenSetBy(objectType, outputArcMeta = arc.arcMeta)
                }
            }
        }
    }
}
