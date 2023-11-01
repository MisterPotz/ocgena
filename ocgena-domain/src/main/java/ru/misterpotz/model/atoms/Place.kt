package ru.misterpotz.model.atoms

import model.*
import ru.misterpotz.input.converter.ext.arcIdConnectedTo

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

    override fun getArcTo(node: PetriAtomId) : PetriAtomId {
        if (node in toTransitions) {
            return id.arcIdConnectedTo(node)
        }
        throw IllegalArgumentException("$node is not in destinations of ${this.id}")
    }

    override fun getArcFrom(node: PetriAtomId) : PetriAtomId {
        if (node in fromTransitions) {
            return node.arcIdConnectedTo(id)
        }
        throw IllegalArgumentException("$node is not in destinations of ${this.id}")
    }

    override fun copyWithoutConnections(): PetriNode {
        return copy(
            fromTransitions = mutableListOf(),
            toTransitions = mutableListOf(),
        )
    }

    override fun isSameType(other: PetriNode): Boolean {
        return other is Place
    }
}
