package ru.misterpotz.ocgena.registries.typel

import ru.misterpotz.expression.paramspace.VariableParameterSpace
import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.*
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.registries.ArcsMultiplicityDelegate
import ru.misterpotz.ocgena.registries.typea.ArcToMultiplicityNormalDelegateTypeA
import ru.misterpotz.ocgena.simulation.binding.TokenSet
import ru.misterpotz.ocgena.simulation.binding.buffer.TokenGroupedInfo
import ru.misterpotz.ocgena.simulation.interactors.TokenAmountStorage
import javax.inject.Inject

class ArcToMultiplicityVariableDelegateTypeL @Inject constructor(
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
    private val arcToMultiplicityNormalDelegateTypeA: ArcToMultiplicityNormalDelegateTypeA,
    ocNet: OCNet,
) : ArcsMultiplicityDelegate() {
    private val inputArcMultiplicityCache: MutableMap<PetriAtomId, InputArcMultiplicityDynamic> = mutableMapOf()
    private val outputArcMultiplicityCache : MutableMap<PetriAtomId, OutputArcMultiplicityDynamic> = mutableMapOf()
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
        tokenGroupedInfo: TokenGroupedInfo,
        arc: Arc,
    ): OutputArcMultiplicity {
        require(arc is VariableArc)

        val targetPlace = arc.arrowNodeId!!
        val objectTypeId = placeToObjectTypeRegistry[targetPlace]

        val variableName = arc.variableName
            ?: return arcToMultiplicityNormalDelegateTypeA
                .transitionOutputMultiplicity(tokenGroupedInfo, arc)

        val sourceBatch = tokenGroupedInfo.getTokenSetBy(
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
            tokenSet = sourceBatch,
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

    override fun transitionOutputMultiplicityDynamic(arc: Arc): OutputArcMultiplicityDynamic {
        require(arc is VariableArc)
        if (arc.variableName == null) {
            return arcToMultiplicityNormalDelegateTypeA.transitionOutputMultiplicityDynamic(arc)
        }

        return outputArcMultiplicityCache.getOrPut(arc.id) {
            object : OutputArcMultiplicityDynamic {
                override fun requiredTokenAmount(tokenGroupedInfo: TokenGroupedInfo): Int {

                    val variableName = arc.variableName!!
                    val totalAvailableTokensForArc = getTokenSourceForThisArc(tokenGroupedInfo)!!.size

                    // calculate the amount of tokens needed for this arc
                    val requiredTokensAmount = arc.mathNode!!.evaluate(
                        parameterSpace =
                        VariableParameterSpace(
                            variableName to totalAvailableTokensForArc.toDouble()
                        )
                    )

                    return roundUpIfNeeded(requiredTokensAmount)

                }

                override fun getTokenSourceForThisArc(tokenGroupedInfo: TokenGroupedInfo): TokenSet? {
                    val targetPlace = arc.arrowNodeId!!
                    val objectTypeId = placeToObjectTypeRegistry[targetPlace]

                    return tokenGroupedInfo.getTokenSetBy(
                        toPlaceObjectTypeId = objectTypeId,
                        outputArcMeta = arc.arcMeta
                    )!!
                }
            }
        }
    }
}