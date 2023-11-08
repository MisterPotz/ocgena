package ru.misterpotz.ocgena.registries.delegates

import ru.misterpotz.ocgena.registries.ArcsMultiplicityDelegate
import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcType

class CompoundArcsMultiplicityDelegate(
    private val arcMultiplicityDelegates : Map<ArcType, ArcsMultiplicityDelegate>
) : ArcsMultiplicityDelegate() {
    override fun multiplicity(arc: Arc): ArcMultiplicity {
        return arcMultiplicityDelegates[arc.arcType]!!.multiplicity(arc)
    }
}