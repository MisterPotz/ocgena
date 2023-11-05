package ru.misterpotz.ocgena.registries.typea

import ru.misterpotz.ocgena.registries.ArcsMultiplicityDelegate
import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicityValue
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.simulation.logging.loggers.CurrentSimulationDelegate
import javax.inject.Inject

class ArcToMultiplicityNormalDelegateTypeA @Inject constructor(
    private val currentSimulationDelegate: CurrentSimulationDelegate,
) : ArcsMultiplicityDelegate(), CurrentSimulationDelegate by currentSimulationDelegate {
    override fun multiplicity(arc: Arc): ArcMultiplicity {
        require(arc is NormalArc)

        val requiredTokens = arc.multiplicity
        val inputNodeId = arc.tailNodeId!!

        val tokensAtPlace = pMarking[inputNodeId]?.size ?: 0

        val inputPlaceHasEnoughTokens = tokensAtPlace >= requiredTokens

        return ArcMultiplicityValue(
            inputPlaceHasEnoughTokens,
            arc.multiplicity
        )
    }
}
