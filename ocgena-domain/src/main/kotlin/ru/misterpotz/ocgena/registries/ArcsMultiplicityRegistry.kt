package ru.misterpotz.ocgena.registries

import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc

interface ArcsMultiplicityRegistry {
    fun multiplicity(arcId: PetriAtomId): ArcMultiplicity
}

abstract class ArcsMultiplicityDelegate() {
    abstract fun multiplicity(arc: Arc): ArcMultiplicity
}

internal class ArcsMultiplicityRegistryDelegating(
    private val arcsRegistry: ArcsRegistry,
    private val arcsMultiplicityDelegate: ArcsMultiplicityDelegate
) : ArcsMultiplicityRegistry {
    override fun multiplicity(arcId: PetriAtomId): ArcMultiplicity {
        val arc = arcsRegistry[arcId]
        return arcsMultiplicityDelegate.multiplicity(arc)
    }
}
