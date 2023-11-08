package ru.misterpotz.ocgena.registries.typea

import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicityValue
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.registries.ArcsMultiplicityDelegate
import ru.misterpotz.ocgena.simulation.logging.loggers.CurrentSimulationDelegate
import ru.misterpotz.ocgena.simulation.state.PMarkingProvider

class ArcToMultiplicityVariableDelegateTypeA(
    private val pMarkingProvider: PMarkingProvider,
) : ArcsMultiplicityDelegate() {
    private val pMarking = pMarkingProvider.get()
    override fun multiplicity(arc: Arc): ArcMultiplicity {
        require(arc is VariableArc)

        val marking = pMarking[arc.tailNodeId!!]

        return ArcMultiplicityValue(
            inputPlaceHasEnoughTokens = (marking?.size ?: 0) > 1,
            requiredTokenAmount = marking?.size ?: 0
        )
    }
}
