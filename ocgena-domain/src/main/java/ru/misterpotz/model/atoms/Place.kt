package ru.misterpotz.model.atoms

import model.*
import ru.misterpotz.model.ext.arcIdTo
import ru.misterpotz.model.validation.PetriAtomVisitorDFS

data class Place(
    override val id: PlaceId,
    override val label: String,
    val fromTransitions: MutableList<PetriAtomId> = mutableListOf(),
    val toTransitions: MutableList<PetriAtomId> = mutableListOf()
) : PetriNode, LabelHolder, ConsistencyCheckable {
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
