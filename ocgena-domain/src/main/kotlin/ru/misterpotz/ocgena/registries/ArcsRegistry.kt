package model

import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Place
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.registries.PetriAtomRegistry

interface WithTail {
    fun to(node: PetriAtomId): Arc
}

interface WithArrow {
    fun from(node: PetriAtomId): Arc
}

interface ArcsRegistry {
    fun withTail(placeId: PetriAtomId): WithTail
    fun withArrow(placeId: PetriAtomId): WithArrow

    operator fun get(arcId: PetriAtomId) : Arc
    fun multiplicity(arcId: PetriAtomId) : ArcMultiplicity

    val iterable: Iterable<Arc>
    val size : Int

}

fun ArcsRegistry(petriAtomRegistry: PetriAtomRegistry) : ArcsRegistry {
    return ArcsRegistryMap(petriAtomRegistry)
}

internal class ArcsRegistryMap(
    private val petriAtomRegistry: PetriAtomRegistry
) : ArcsRegistry {
    private fun tailToArrow(tail: PetriAtomId, arrow: PetriAtomId): Arc {
        return petriAtomRegistry[tail.arcIdTo(arrow)] as Arc
    }

    override fun withTail(placeId: PetriAtomId): WithTail {
        return WithTailImpl(placeId)
    }

    override fun withArrow(placeId: PetriAtomId): WithArrow {
        return WithArrowImpl(placeId)
    }

    override fun get(arcId: PetriAtomId): Arc {
        return petriAtomRegistry[arcId] as Arc
    }

    override fun multiplicity(arcId: PetriAtomId): ArcMultiplicity {
        return
    }

    override val iterable: Iterable<Arc> = petriAtomRegistry
        .getArcs()
        .map { petriAtomRegistry.getArc(it) }

    override val size: Int
        get() = iterable.count()

    private inner class WithTailImpl(private val tail: PetriAtomId) : WithTail {
        override fun to(node: PetriAtomId): Arc {
            return tailToArrow(tail, node)
        }
    }

    private inner class WithArrowImpl(private val arrow: PetriAtomId) : WithArrow {
        override fun from(node: PetriAtomId): Arc {
            return tailToArrow(node, arrow = arrow)
        }
    }
}
