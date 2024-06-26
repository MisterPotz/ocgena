package ru.misterpotz.ocgena.registries

import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo

interface WithTail {
    fun to(node: PetriAtomId): Arc
}

interface WithArrow {
    fun from(node: PetriAtomId): Arc
}


interface ArcsRegistry {
    fun withTail(placeId: PetriAtomId): WithTail
    fun withArrow(placeId: PetriAtomId): WithArrow

    operator fun get(arcId: PetriAtomId): Arc

    val iterable: Iterable<Arc>
    val size: Int

}

fun ArcsRegistry(
    petriAtomRegistry: PetriAtomRegistry,
): ArcsRegistry {
    return ArcsRegistryMap(petriAtomRegistry)
}

internal class ArcsRegistryMap(
    private val petriAtomRegistry: PetriAtomRegistry,
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

    override val iterable: List<Arc> by lazy(LazyThreadSafetyMode.NONE) {
        petriAtomRegistry
            .getArcs()
            .map { petriAtomRegistry.getArc(it) }
    }

    override val size: Int
        get() = iterable.size

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
