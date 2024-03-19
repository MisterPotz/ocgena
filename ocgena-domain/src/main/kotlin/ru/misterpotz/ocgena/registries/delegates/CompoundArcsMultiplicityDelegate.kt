package ru.misterpotz.ocgena.registries.delegates

import ru.misterpotz.ocgena.registries.ArcsMultiplicityDelegate
import ru.misterpotz.ocgena.ocnet.primitives.InputArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.InputArcMultiplicityDynamic
import ru.misterpotz.ocgena.ocnet.primitives.OutputArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.OutputArcMultiplicityDynamic
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcType
import ru.misterpotz.ocgena.simulation.binding.buffer.TokenGroupedInfo

class CompoundArcsMultiplicityDelegate(
    private val arcMultiplicityDelegates: Map<ArcType, ArcsMultiplicityDelegate>
) : ArcsMultiplicityDelegate() {
    override fun transitionInputMultiplicity(arc: Arc): InputArcMultiplicity {
        return arcMultiplicityDelegates[arc.arcType]!!.transitionInputMultiplicity(arc)
    }

    override fun transitionInputMultiplicityDynamic(arc: Arc): InputArcMultiplicityDynamic {
        return arcMultiplicityDelegates[arc.arcType]!!.transitionInputMultiplicityDynamic(arc)
    }

    override fun transitionOutputMultiplicity(
        tokenGroupedInfo: TokenGroupedInfo,
        arc: Arc
    ): OutputArcMultiplicity {
        return arcMultiplicityDelegates[arc.arcType]!!.transitionOutputMultiplicity(
            arc = arc,
            tokenGroupedInfo = tokenGroupedInfo
        )
    }

    override fun transitionOutputMultiplicityDynamic(arc: Arc): OutputArcMultiplicityDynamic {
        return arcMultiplicityDelegates[arc.arcType]!!.transitionOutputMultiplicityDynamic(
            arc = arc
        )
    }
}