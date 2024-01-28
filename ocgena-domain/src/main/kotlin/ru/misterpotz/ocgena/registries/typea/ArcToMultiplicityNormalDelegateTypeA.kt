package ru.misterpotz.ocgena.registries.typea

import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.*
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.registries.ArcsMultiplicityDelegate
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.simulation.binding.TokenSet
import ru.misterpotz.ocgena.simulation.binding.buffer.TokenGroupedInfo
import ru.misterpotz.ocgena.simulation.di.GlobalTokenBunch
import ru.misterpotz.ocgena.simulation.interactors.TokenAmountStorage
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunch
import javax.inject.Inject

class ArcToMultiplicityNormalDelegateTypeA @Inject constructor(
    @GlobalTokenBunch
    private val sparseTokenBunch: SparseTokenBunch,
    private val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
) : ArcsMultiplicityDelegate() {
    private val inputArcMultiplicityCache : MutableMap<PetriAtomId, InputArcMultiplicityDynamic> = mutableMapOf()
    private val outputArcMultiplicityCache : MutableMap<PetriAtomId, OutputArcMultiplicityDynamic> = mutableMapOf()

    override fun transitionInputMultiplicity(arc: Arc): InputArcMultiplicity {
        require(arc is NormalArc)

        val requiredTokens = arc.multiplicity
        val inputNodeId = arc.tailNodeId!!

        val tokensAtPlace = sparseTokenBunch.tokenAmountStorage().getTokensAt(inputNodeId)
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
        tokenGroupedInfo: TokenGroupedInfo,
        arc: Arc
    ): OutputArcMultiplicity {
        require(arc is NormalArc)

        val place = arc.arrowNodeId!!
        val objectType = placeToObjectTypeRegistry[place]

        val tokenBuffer = tokenGroupedInfo.getTokenSetBy(objectType, outputArcMeta = arc.arcMeta)

        return OutputArcMultiplicityValue(
            arc.multiplicity,
            tokenSet = tokenBuffer
        )
    }

    override fun transitionOutputMultiplicityDynamic(arc: Arc): OutputArcMultiplicityDynamic {
        require(arc is NormalArc)

        return outputArcMultiplicityCache.getOrPut(arc.id) {
            object : OutputArcMultiplicityDynamic {
                override fun requiredTokenAmount(tokenGroupedInfo: TokenGroupedInfo): Int {
                    return arc.multiplicity
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
