package model

import ru.misterpotz.input.converter.ext.arcIdConnectedTo
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
}

fun Arcs(arcs: MutableMap<PetriAtomId, Arc> = mutableMapOf()) : Arcs {
    return ArcsMap(arcs)
}

internal class ArcsMap(
    private val arcs: MutableMap<PetriAtomId, Arc> = mutableMapOf()
) : Arcs {
    private fun tailToArrow(tail: PetriAtomId, arrow: PetriAtomId): Arc {
        return arcs[tail.arcIdConnectedTo(arrow)]!!
    }

    override fun withTail(placeId: PetriAtomId): WithTail {
        return WithTailImpl(placeId)
    }

    override fun withArrow(placeId: PetriAtomId): WithArrow {
        return WithArrowImpl(placeId)
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
