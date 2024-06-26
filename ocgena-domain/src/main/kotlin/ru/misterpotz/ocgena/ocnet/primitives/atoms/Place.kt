package ru.misterpotz.ocgena.ocnet.primitives.atoms

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.*
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.validation.PetriAtomVisitorDFS

@Serializable
@SerialName("place")
data class Place(
    override val id: PetriAtomId,
    override val label: String,
    @SerialName("from_transtns")
    val fromTransitions: MutableList<PetriAtomId> = mutableListOf(),
    @SerialName("to_transtns")
    val toTransitions: MutableList<PetriAtomId> = mutableListOf(),
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
