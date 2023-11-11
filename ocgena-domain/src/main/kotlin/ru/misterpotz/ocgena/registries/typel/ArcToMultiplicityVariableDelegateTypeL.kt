package ru.misterpotz.ocgena.registries.typel

import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicityValue
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.registries.ArcsMultiplicityDelegate
import ru.misterpotz.ocgena.registries.typea.ArcToMultiplicityNormalDelegateTypeA
import javax.inject.Inject

class ArcToMultiplicityVariableDelegateTypeL @Inject constructor(
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
    private val arcToMultiplicityNormalDelegateTypeA: ArcToMultiplicityNormalDelegateTypeA,
) : ArcsMultiplicityDelegate() {
    override fun multiplicity(arc: Arc): ArcMultiplicity {
        require(arc is VariableArc)

        // use fallback
        val variable = arc.variableName
            ?: return arcToMultiplicityNormalDelegateTypeA.multiplicity(arc)

        val realTokenAmount = objectTokenRealAmountRegistry.getRealAmountAt(arc.tailNodeId!!)

        return ArcMultiplicityValue(
            inputPlaceHasEnoughTokens = (realTokenAmount) > 1,
            requiredTokenAmount = realTokenAmount // require as much tokens as there are
        )
    }
}
