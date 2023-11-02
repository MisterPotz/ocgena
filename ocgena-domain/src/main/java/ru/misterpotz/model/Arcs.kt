package model

import ru.misterpotz.model.collections.PetriAtomRegistry
import ru.misterpotz.model.ext.arcIdTo
import ru.misterpotz.model.atoms.Arc


interface WithTail {
    fun to(node: PetriAtomId): Arc
}

interface WithArrow {
    fun from(node: PetriAtomId): Arc
}

interface Arcs {
    fun withTail(placeId: PetriAtomId): WithTail
    fun withArrow(placeId: PetriAtomId): WithArrow
    operator fun get(arcId : PetriAtomId) : Arc
}

fun Arcs(petriAtomRegistry: PetriAtomRegistry) : Arcs {
    return ArcsMap(petriAtomRegistry)
}

internal class ArcsMap(
    private val petriAtomRegistry: PetriAtomRegistry
) : Arcs {
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
