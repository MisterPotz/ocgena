package ru.misterpotz.ocgena.registries.typea

import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicityValue
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.registries.ArcsMultiplicityDelegate
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.simulation.typea.TokenBuffer
import ru.misterpotz.ocgena.simulation.typea.TransitionBufferInfo
import javax.inject.Inject

class ArcToMultiplicityNormalDelegateTypeA @Inject constructor(
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
    private val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
) : ArcsMultiplicityDelegate() {

    override fun transitionInputMultiplicity(arc: Arc): ArcMultiplicity {
        require(arc is NormalArc)

        val requiredTokens = arc.multiplicity
        val inputNodeId = arc.tailNodeId!!

        val tokensAtPlace = objectTokenRealAmountRegistry.getRealAmountAt(inputNodeId)
        val inputPlaceHasEnoughTokens = tokensAtPlace >= requiredTokens

        return ArcMultiplicityValue(
            inputPlaceHasEnoughTokens,
            arc.multiplicity
        )
    }

    override fun transitionOutputMultiplicity(transitionBufferInfo: TransitionBufferInfo, arc: Arc): ArcMultiplicity {
        require(arc is NormalArc)

        val requiredTokens = arc.multiplicity

        val place = arc.arrowNodeId!!
        val objectType = placeToObjectTypeRegistry[place]

        val tokenBuffer = transitionBufferInfo.getBatchBy(objectType)
        val sourceNodeItemsSize = tokenBuffer.size
        val transitionBufferHasEnoughTokens = sourceNodeItemsSize >= requiredTokens

        return ArcMultiplicityValue(
            transitionBufferHasEnoughTokens,
            arc.multiplicity
        )
    }
}
