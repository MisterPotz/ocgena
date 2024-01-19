package ru.misterpotz.ocgena.registries.typel

import ru.misterpotz.expression.paramspace.VariableParameterSpace
import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.InputArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.InputArcMultiplicityDynamic
import ru.misterpotz.ocgena.ocnet.primitives.InputArcMultiplicityValue
import ru.misterpotz.ocgena.ocnet.primitives.OutputArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.OutputArcMultiplicityValue
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.registries.ArcsMultiplicityDelegate
import ru.misterpotz.ocgena.registries.typea.ArcToMultiplicityNormalDelegateTypeA
import ru.misterpotz.ocgena.simulation.binding.buffer.TransitionGroupedTokenInfo
import ru.misterpotz.ocgena.simulation.interactors.TokenAmountStorage
import javax.inject.Inject

class ArcToMultiplicityVariableDelegateTypeL @Inject constructor(
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
    private val arcToMultiplicityNormalDelegateTypeA: ArcToMultiplicityNormalDelegateTypeA,
    ocNet: OCNet,
) : ArcsMultiplicityDelegate() {
    private val inputArcMultiplicityCache: MutableMap<PetriAtomId, InputArcMultiplicityDynamic> = mutableMapOf()
    private val placeToObjectTypeRegistry = ocNet.placeToObjectTypeRegistry
    override fun transitionInputMultiplicity(arc: Arc): InputArcMultiplicity {
        require(arc is VariableArc)

        // use fallback
        arc.variableName
            ?: return arcToMultiplicityNormalDelegateTypeA.transitionInputMultiplicity(arc)

        val realTokenAmount = objectTokenRealAmountRegistry.getRealAmountAt(arc.tailNodeId!!)

        return InputArcMultiplicityValue(
            sourceNodeHasEnoughTokens = (realTokenAmount) > 1,
            requiredTokenAmount = realTokenAmount // require as much tokens as there are
        )
    }

    override fun transitionInputMultiplicityDynamic(arc: Arc): InputArcMultiplicityDynamic {
        require(arc is VariableArc)
        if (arc.variableName == null)
            return arcToMultiplicityNormalDelegateTypeA.transitionInputMultiplicityDynamic(arc)

        val inputNodeId = arc.tailNodeId!!

        return inputArcMultiplicityCache.getOrPut(arc.id) {
            object : InputArcMultiplicityDynamic {
                override fun inputPlaceHasEnoughTokens(tokenAmountStorage: TokenAmountStorage): Boolean {
                    return tokenAmountStorage.getTokensAt(inputNodeId) >= 1
                }

                override fun requiredTokenAmount(tokenAmountStorage: TokenAmountStorage): Int {
                    return tokenAmountStorage.getTokensAt(inputNodeId)
                }
            }
        }
    }


    override fun transitionOutputMultiplicity(
        transitionGroupedTokenInfo: TransitionGroupedTokenInfo,
        arc: Arc,
    ): OutputArcMultiplicity {
        require(arc is VariableArc)

        val targetPlace = arc.arrowNodeId!!
        val objectTypeId = placeToObjectTypeRegistry[targetPlace]

        val variableName = arc.variableName
            ?: return arcToMultiplicityNormalDelegateTypeA
                .transitionOutputMultiplicity(transitionGroupedTokenInfo, arc)

        val sourceBatch = transitionGroupedTokenInfo.getGroup(
            toPlaceObjectTypeId = objectTypeId,
            outputArcMeta = arc.arcMeta
        )!!
        val totalAvailableTokensForArc = sourceBatch.size

        // calculate the amount of tokens needed for this arc
        val requiredTokensAmount = arc.mathNode!!.evaluate(
            parameterSpace =
            VariableParameterSpace(
                variableName to totalAvailableTokensForArc.toDouble()
            )
        )

        val requiredTokenAmount = roundUpIfNeeded(requiredTokensAmount)

        return OutputArcMultiplicityValue(
            tokenGroup = sourceBatch,
            requiredTokenAmount = requiredTokenAmount,
        )
    }

    private fun roundUpIfNeeded(value: Double): Int {
        return if (value % 1.0 != 0.0) {
            kotlin.math.ceil(value).toInt()
        } else {
            value.toInt()
        }
    }
}
