package model.typea

import kotlinx.serialization.Serializable
import model.Arc
import model.PetriNode
import model.SerializableAtom

@Serializable
data class SerializableVariableArcTypeA(
    val fromId : String?,
    val toId : String?,
) : SerializableAtom

data class VariableArcTypeA(
    override val id: String,
    override var arrowNode: PetriNode?,
    override var tailNode: PetriNode?,
    // TODO: sets up the allowed multiplicity dynamically, probably needs some parameters
//    private val _multiplicity : () -> Int,
) : Arc() {

    override val serializableAtom: SerializableAtom by lazy {
        SerializableVariableArcTypeA(fromId = tailNode?.id, toId = arrowNode?.id)
    }

    override fun copyWithTailAndArrow(newTail: PetriNode, newArrow: PetriNode): Arc {
        return copy(
            arrowNode = newArrow,
            tailNode = newTail
        )
    }

    override fun isSameArcType(other: Arc): Boolean {
        return other is VariableArcTypeA
    }

    override fun toString(): String {
        return "[ ${tailNode?.id} ] => [ ${arrowNode?.id} ]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as VariableArcTypeA

        if (id != other.id) return false
        if (arrowNode?.id != other.arrowNode?.id) return false
        return tailNode?.id == other.tailNode?.id
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (arrowNode?.id?.hashCode() ?: 0)
        result = 31 * result + (tailNode?.id?.hashCode() ?: 0)
        return result
    }
}
