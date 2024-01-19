package ru.misterpotz.ocgena.registries.typea

import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.InputArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.InputArcMultiplicityDynamic
import ru.misterpotz.ocgena.ocnet.primitives.InputArcMultiplicityValue
import ru.misterpotz.ocgena.ocnet.primitives.OutputArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.OutputArcMultiplicityValue
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArc
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArcMetaTypeA
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.registries.ArcsMultiplicityDelegate
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.simulation.binding.buffer.TransitionGroupedTokenInfo
import ru.misterpotz.ocgena.simulation.interactors.TokenAmountStorage
import javax.inject.Inject

class ArcToMultiplicityVariableDelegateTypeA @Inject constructor(
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
    ocNet: OCNet,
) : ArcsMultiplicityDelegate() {
    private val inputArcMultiplicityCache : MutableMap<PetriAtomId, InputArcMultiplicityDynamic> = mutableMapOf()
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
        transitionGroupedTokenInfo: TransitionGroupedTokenInfo,
        arc: Arc
    ): OutputArcMultiplicity {
        require(arc is VariableArc)

        val targetPlace = arc.arrowNodeId!!
        val objectTypeId = placeToObjectTypeRegistry[targetPlace]
        val batchForType = transitionGroupedTokenInfo.getGroup(
            toPlaceObjectTypeId = objectTypeId,
            outputArcMeta = VariableArcMetaTypeA
        )!!
        val tokensAtBuffer = batchForType.size

        return OutputArcMultiplicityValue(
            requiredTokenAmount = tokensAtBuffer,
            tokenGroup = batchForType,
        )
    }
}
