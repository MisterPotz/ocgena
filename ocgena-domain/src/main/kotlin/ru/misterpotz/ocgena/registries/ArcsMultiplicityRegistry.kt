package ru.misterpotz.ocgena.registries

import ru.misterpotz.ocgena.ocnet.primitives.InputArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.InputArcMultiplicityDynamic
import ru.misterpotz.ocgena.ocnet.primitives.OutputArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.simulation.binding.buffer.TransitionGroupedTokenInfo

interface ArcsMultiplicityRegistry {
    fun transitionInputMultiplicity(arcId: PetriAtomId): InputArcMultiplicity
    fun transitionInputMultiplicityDynamic(arcId: PetriAtomId) : InputArcMultiplicityDynamic
    fun transitionOutputMultiplicity(
        transitionGroupedTokenInfo: TransitionGroupedTokenInfo,
        arcId: PetriAtomId
    ): OutputArcMultiplicity
}

abstract class ArcsMultiplicityDelegate {
    abstract fun transitionInputMultiplicity(arc: Arc): InputArcMultiplicity

    abstract fun transitionInputMultiplicityDynamic(arc: Arc) : InputArcMultiplicityDynamic

    abstract fun transitionOutputMultiplicity(
        transitionGroupedTokenInfo: TransitionGroupedTokenInfo,
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

    override fun transitionInputMultiplicityDynamic(arcId: PetriAtomId): InputArcMultiplicityDynamic {
        val arc = arcsRegistry[arcId]
        return arcsMultiplicityDelegate.transitionInputMultiplicityDynamic(arc)
    }

    override fun transitionOutputMultiplicity(
        transitionGroupedTokenInfo: TransitionGroupedTokenInfo,
        arcId: PetriAtomId
    ): OutputArcMultiplicity {
        val arc = arcsRegistry[arcId]
        return arcsMultiplicityDelegate.transitionOutputMultiplicity(
            transitionGroupedTokenInfo,
            arc
        )
    }
}
