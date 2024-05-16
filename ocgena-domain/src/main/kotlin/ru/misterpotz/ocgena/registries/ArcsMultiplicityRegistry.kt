package ru.misterpotz.ocgena.registries

import ru.misterpotz.ocgena.ocnet.primitives.*
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.simulation_old.binding.buffer.TokenGroupedInfo

interface ArcsMultiplicityRegistry {
    fun transitionInputMultiplicity(arcId: PetriAtomId): InputArcMultiplicity
    fun transitionInputMultiplicityDynamic(arcId: PetriAtomId): InputArcMultiplicityDynamic
    fun transitionOutputMultiplicity(
        tokenGroupedInfo: TokenGroupedInfo,
        arcId: PetriAtomId
    ): OutputArcMultiplicity

    fun transitionOutputMultiplicityDynamic(
        arcId: PetriAtomId
    ): OutputArcMultiplicityDynamic

}

abstract class ArcsMultiplicityDelegate {
    abstract fun transitionInputMultiplicity(arc: Arc): InputArcMultiplicity

    abstract fun transitionInputMultiplicityDynamic(arc: Arc): InputArcMultiplicityDynamic

    abstract fun transitionOutputMultiplicity(
        tokenGroupedInfo: TokenGroupedInfo,
        arc: Arc
    ): OutputArcMultiplicity

    abstract fun transitionOutputMultiplicityDynamic(
        arc: Arc
    ): OutputArcMultiplicityDynamic
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
        tokenGroupedInfo: TokenGroupedInfo,
        arcId: PetriAtomId
    ): OutputArcMultiplicity {
        val arc = arcsRegistry[arcId]
        return arcsMultiplicityDelegate.transitionOutputMultiplicity(
            tokenGroupedInfo,
            arc
        )
    }

    override fun transitionOutputMultiplicityDynamic(arcId: PetriAtomId): OutputArcMultiplicityDynamic {
        val arc = arcsRegistry[arcId]
        return arcsMultiplicityDelegate.transitionOutputMultiplicityDynamic(arc)
    }
}
