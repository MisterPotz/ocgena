package ru.misterpotz.ocgena.registries

import ru.misterpotz.ocgena.ocnet.primitives.InputArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.OutputArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.simulation.binding.buffer.TransitionBufferInfo

interface ArcsMultiplicityRegistry {
    fun transitionInputMultiplicity(arcId: PetriAtomId): InputArcMultiplicity
    fun transitionOutputMultiplicity(
        transitionBufferInfo: TransitionBufferInfo,
        arcId: PetriAtomId
    ): OutputArcMultiplicity
}

abstract class ArcsMultiplicityDelegate {
    abstract fun transitionInputMultiplicity(arc: Arc): InputArcMultiplicity
    abstract fun transitionOutputMultiplicity(
        transitionBufferInfo: TransitionBufferInfo,
        arc: Arc
    ): OutputArcMultiplicity
}

internal class ArcsMultiplicityRegistryDelegating(
    private val arcsRegistry: ArcsRegistry,
    private val arcsMultiplicityDelegate: ArcsMultiplicityDelegate
) : ArcsMultiplicityRegistry {
    override fun transitionInputMultiplicity(arcId: PetriAtomId): InputArcMultiplicity {
        val arc = arcsRegistry[arcId]
        return arcsMultiplicityDelegate.transitionInputMultiplicity(arc)
    }

    override fun transitionOutputMultiplicity(
        transitionBufferInfo: TransitionBufferInfo,
        arcId: PetriAtomId
    ): OutputArcMultiplicity {
        val arc = arcsRegistry[arcId]
        return arcsMultiplicityDelegate.transitionOutputMultiplicity(
            transitionBufferInfo,
            arc
        )
    }
}
