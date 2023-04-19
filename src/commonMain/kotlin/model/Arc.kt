package model

abstract class Arc : ConsistencyCheckable, PetriAtom {
    open var arrowNode: PetriNode? = null
    open var tailNode: PetriNode? = null

    override fun acceptVisitor(visitor: PetriAtomVisitorDFS) {
        visitor.visitArc(this)
    }

    override var subgraphIndex: Int = PetriAtom.UNASSIGNED_SUBGRAPH_INDEX

    fun requireTailPlace(): Place {
        return checkNotNull(tailNode as? Place) {
            "tail place was required not null"
        }
    }

    fun requireTailTransition(): Transition {
        return checkNotNull(tailNode as? Transition) {
            "tail transition was required to be not null"
        }
    }

    fun requireArrowTransition(): Transition {
        return checkNotNull(arrowNode as? Transition) {
            "tail transition was required to be not null"
        }
    }

    fun requireArrowPlace(): Place {
        return checkNotNull(arrowNode as? Place) {
            "arrow place was required not null"
        }
    }

    abstract fun tailPlaceHasEnoughTokens(): Boolean
    abstract fun isSameArcType(other: Arc): Boolean

    abstract fun copyWithTailAndArrow(
        newTail: PetriNode,
        newArrow: PetriNode,
    ): Arc
}

data class NormalArc(
    override val id: String,
    override var arrowNode: PetriNode?,
    override var tailNode: PetriNode?,
    val multiplicity: Int = 1,
) : Arc() {
    override fun tailPlaceHasEnoughTokens(): Boolean {
        return (tailNode!! as Place).tokens >= multiplicity
    }

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
        return "->"
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

data class VariableArc(
    override val id: String,
    override var arrowNode: PetriNode?,
    override var tailNode: PetriNode?,
    // TODO: sets up the allowed multiplicity dynamically, probably needs some parameters
//    private val _multiplicity : () -> Int,
) : Arc() {
    //    override val multiplicity : Int
//        get() = _multiplicity()
    override fun tailPlaceHasEnoughTokens(): Boolean {
//        val multiplicity = _multiplicity()
        return (tailNode!! as Place).tokens >= 1
    }

    override fun copyWithTailAndArrow(newTail: PetriNode, newArrow: PetriNode): Arc {
        return copy(
            arrowNode = newArrow,
            tailNode = newTail
        )
    }

    override fun isSameArcType(other: Arc): Boolean {
        return other is VariableArc
    }

    override fun toString(): String {
        return "=>"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as VariableArc

        if (id != other.id) return false
        if (arrowNode?.id != other.arrowNode?.id) return false
        if (tailNode?.id != other.tailNode?.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (arrowNode?.id?.hashCode() ?: 0)
        result = 31 * result + (tailNode?.id?.hashCode() ?: 0)
        return result
    }
}
