package ru.misterpotz.ocgena.registries.delegates

import ru.misterpotz.ocgena.registries.ArcsMultiplicityDelegate
import ru.misterpotz.ocgena.ocnet.primitives.InputArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcType

class CompoundArcsMultiplicityDelegate(
    private val arcMultiplicityDelegates : Map<ArcType, ArcsMultiplicityDelegate>
) : ArcsMultiplicityDelegate() {
    override fun transitionInputMultiplicity(arc: Arc): InputArcMultiplicity {
        return arcMultiplicityDelegates[arc.arcType]!!.transitionInputMultiplicity(arc)
    }
}