package model

data class NormalArc(
    override val id: String,
    override var arrowNode: PetriNode?,
    override var tailNode: PetriNode?,
    val multiplicity: Int = 1,
) : Arc() {

    override fun isSameArcType(other: Arc): Boolean {
        return other is NormalArc
    }

    override fun copyWithTailAndArrow(newTail: PetriNode, newArrow: PetriNode): Arc {
        return copy(
            arrowNode = newArrow,
            tailNode = newTail,
        )
    }

    override fun toString(): String {
        return "[ ${tailNode?.id} ] -> [ ${arrowNode?.id} ]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as NormalArc

        if (id != other.id) return false
        if (arrowNode?.id != other.arrowNode?.id) return false
        if (tailNode?.id != other.tailNode?.id) return false
        if (multiplicity != other.multiplicity) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (arrowNode?.id?.hashCode() ?: 0)
        result = 31 * result + (tailNode?.id?.hashCode() ?: 0)
        result = 31 * result + multiplicity
        return result
    }
}
