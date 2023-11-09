package ru.misterpotz.ocgena.registries.typea

import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicityValue
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.registries.ArcsMultiplicityDelegate
import javax.inject.Inject

class ArcToMultiplicityNormalDelegateTypeA @Inject constructor(
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry
) : ArcsMultiplicityDelegate() {

    override fun multiplicity(arc: Arc): ArcMultiplicity {
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
}
