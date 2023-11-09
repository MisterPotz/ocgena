package ru.misterpotz.ocgena.registries.typea

import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicityValue
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.registries.ArcsMultiplicityDelegate
import javax.inject.Inject

class ArcToMultiplicityVariableDelegateTypeA @Inject constructor(
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry
) : ArcsMultiplicityDelegate() {
    override fun multiplicity(arc: Arc): ArcMultiplicity {
        require(arc is VariableArc)

        val realTokenAmount = objectTokenRealAmountRegistry.getRealAmountAt(arc.tailNodeId!!)

        return ArcMultiplicityValue(
            inputPlaceHasEnoughTokens = (realTokenAmount) > 1,
            requiredTokenAmount = realTokenAmount
        )
    }
}
