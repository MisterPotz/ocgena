package ru.misterpotz.ocgena.ocnet.primitives.atoms

import ru.misterpotz.ocgena.ocnet.PlaceId
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.ocnet.primitives.LabelHolder
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtom
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.PetriNode
import ru.misterpotz.ocgena.validation.PetriAtomVisitorDFS

data class Place(
    override val id: PlaceId,
    override val label: String,
    val fromTransitions: MutableList<PetriAtomId> = mutableListOf(),
    val toTransitions: MutableList<PetriAtomId> = mutableListOf()
) : PetriNode, LabelHolder {
    fun addFromTransition(transition: PetriAtomId) {
        fromTransitions.add(transition)
    }

    fun addToTransition(transition: PetriAtomId) {
        toTransitions.add(transition)
    }

    override fun acceptVisitor(visitor: PetriAtomVisitorDFS) {
        visitor.visitPlace(this)
    }

    override fun isSameType(other: PetriAtom): Boolean {
        return other is Place
    }

    override fun getArcTo(node: PetriAtomId) : PetriAtomId {
        if (node in toTransitions) {
            return id.arcIdTo(node)
        }
        throw IllegalArgumentException("$node is not in destinations of ${this.id}")
    }

    override fun getArcFrom(node: PetriAtomId) : PetriAtomId {
        if (node in fromTransitions) {
            return node.arcIdTo(id)
        }
        throw IllegalArgumentException("$node is not in destinations of ${this.id}")
    }

    override fun copyWithoutConnections(): PetriNode {
        return copy(
            fromTransitions = mutableListOf(),
            toTransitions = mutableListOf(),
        )
    }
}
