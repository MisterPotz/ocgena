package ru.misterpotz.ocgena.ocnet.primitives.atoms

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.*
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.validation.PetriAtomVisitorDFS
import java.lang.IllegalArgumentException

@Deprecated("use PetriAtomID")
typealias TransitionId = String

@Serializable
@SerialName("transition")
data class Transition(
    override val id: PetriAtomId,
    override val label: String,
    @SerialName("from_places")
    val fromPlaces: MutableList<PetriAtomId> = mutableListOf(),
    @SerialName("to_places")
    val toPlaces: MutableList<PetriAtomId> = mutableListOf(),
) : PetriNode, LabelHolder {
    fun addFromPlace(place: PetriAtomId) {
        fromPlaces.add(place)
    }

    fun addToPlace(place: PetriAtomId) {
        toPlaces.add(place)
    }

    fun neighbourhood(): Iterable<PetriAtomId> {
        return sequence {
            yieldAll(fromPlaces)
            yieldAll(toPlaces)
        }.asIterable()
    }

    override fun isSameType(other: PetriAtom): Boolean {
        return other is Transition
    }

    override fun getArcTo(node: PetriAtomId): PetriAtomId {
        if (node in toPlaces) {
            return id.arcIdTo(node)
        }
        throw IllegalArgumentException("$node is not in destinations of ${this.id}")
    }

    override fun getArcFrom(node: PetriAtomId): PetriAtomId {
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
        return String.format(
            "trans. $id pre:$fromPlaces / post:$toPlaces"
        )
    }


}
