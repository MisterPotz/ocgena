package ru.misterpotz.ocgena.ocnet.primitives.atoms

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.ocnet.primitives.LabelHolder
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtom
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.PetriNode
import ru.misterpotz.ocgena.validation.PetriAtomVisitorDFS
import java.lang.IllegalArgumentException

typealias TransitionId = String

@Serializable
data class Transition(
    override val id: TransitionId,
    override val label: String,
    val fromPlaces: MutableList<PetriAtomId> = mutableListOf(),
    val toPlaces: MutableList<PetriAtomId> = mutableListOf(),
) : PetriNode, LabelHolder {
    val inputPlaces: List<PetriAtomId> get() = fromPlaces
    val outputPlaces: List<PetriAtomId> get() = toPlaces

    fun addFromPlace(place: PetriAtomId) {
        fromPlaces.add(place)
    }

    fun addToPlace(place: PetriAtomId) {
        toPlaces.add(place)
    }

    override fun isSameType(other: PetriAtom): Boolean {
        return other is Transition
    }

    override fun getArcTo(node: PetriAtomId) : PetriAtomId {
        if (node in toPlaces) {
            return id.arcIdTo(node)
        }
        throw IllegalArgumentException("$node is not in destinations of ${this.id}")
    }

    override fun getArcFrom(node: PetriAtomId) : PetriAtomId {
        if (node in fromPlaces) {
            return node.arcIdTo(id)
        }
        throw IllegalArgumentException("$node is not in destinations of ${this.id}")
    }

    override fun acceptVisitor(visitor: PetriAtomVisitorDFS) {
        visitor.visitTransition(this)
    }

    override fun copyWithoutConnections(): PetriNode {
        return copy(
            fromPlaces = mutableListOf(),
            toPlaces = mutableListOf()
        )
    }

    override fun toString(): String {
        return "transition [ $label ]"
    }
}
