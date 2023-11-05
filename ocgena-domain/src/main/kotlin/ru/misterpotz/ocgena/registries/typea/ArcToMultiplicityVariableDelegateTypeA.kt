package ru.misterpotz.ocgena.registries.typea

import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicityValue
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.registries.ArcsMultiplicityDelegate
import ru.misterpotz.ocgena.simulation.logging.loggers.CurrentSimulationDelegate

class ArcToMultiplicityVariableDelegateTypeA(
    private val currentSimulationDelegate: CurrentSimulationDelegate,
) : ArcsMultiplicityDelegate(), CurrentSimulationDelegate by currentSimulationDelegate {
    override fun multiplicity(arc: Arc): ArcMultiplicity {
        require(arc is VariableArc)

        val marking = pMarking[arc.tailNodeId!!]

        return ArcMultiplicityValue(
            inputPlaceHasEnoughTokens = (marking?.size ?: 0) > 1,
            requiredTokenAmount = marking?.size ?: 0
        )
    }
}
