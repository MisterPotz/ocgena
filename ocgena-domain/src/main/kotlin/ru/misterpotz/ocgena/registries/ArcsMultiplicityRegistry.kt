package ru.misterpotz.ocgena.registries

import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.simulation.typea.TransitionBufferInfo
import ru.misterpotz.ocgena.simulation.typea.TokenBuffer

interface ArcsMultiplicityRegistry {
    fun transitionInputMultiplicity(arcId: PetriAtomId): ArcMultiplicity
    fun transitionOutputMultiplicity(transitionBufferInfo: TransitionBufferInfo, arcId: PetriAtomId): ArcMultiplicity
}

abstract class ArcsMultiplicityDelegate() {
    abstract fun transitionInputMultiplicity(arc: Arc): ArcMultiplicity
    abstract fun transitionOutputMultiplicity(transitionBufferInfo: TransitionBufferInfo, arc: Arc): ArcMultiplicity
}

internal class ArcsMultiplicityRegistryDelegating(
    private val arcsRegistry: ArcsRegistry,
    private val arcsMultiplicityDelegate: ArcsMultiplicityDelegate
) : ArcsMultiplicityRegistry {
    override fun transitionInputMultiplicity(arcId: PetriAtomId): ArcMultiplicity {
        val arc = arcsRegistry[arcId]
        return arcsMultiplicityDelegate.transitionInputMultiplicity(arc)
    }

    override fun transitionOutputMultiplicity(
        transitionBufferInfo: TransitionBufferInfo,
        arcId: PetriAtomId
    ): ArcMultiplicity {
        val arc = arcsRegistry[arcId]
        return arcsMultiplicityDelegate.transitionOutputMultiplicity(transitionBufferInfo, arc)
    }
}
