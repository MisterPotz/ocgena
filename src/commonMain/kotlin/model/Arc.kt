package model

abstract class Arc : ConsistencyCheckable, PetriAtom {
    open var arrowNode : PetriNode? = null
    open var tailNode : PetriNode? = null

    override fun acceptVisitor(visitor: PetriAtomVisitor) {
        visitor.visitArc(this)
    }

    override var subgraphIndex: Int = PetriAtom.UNASSIGNED_SUBGRAPH_INDEX

    fun requireTailPlace() : Place {
        return checkNotNull(tailNode as? Place) {
            "tail place was required not null"
        }
    }

    fun requireTailTransition() : Transition {
        return checkNotNull(tailNode as? Transition) {
            "tail transition was required to be not null"
        }
    }

    fun requireArrowTransition() : Transition {
        return checkNotNull(arrowNode as? Transition) {
            "tail transition was required to be not null"
        }
    }

    fun requireArrowPlace() : Place {
        return checkNotNull(arrowNode as? Place) {
            "arrow place was required not null"
        }
    }

    abstract fun tailPlaceHasEnoughTokens() : Boolean
    abstract fun isSameArcType(other : Arc) : Boolean
}

class NormalArc(
    override var arrowNode: PetriNode?,
    override var tailNode: PetriNode?,
    val multiplicity: Int = 1
) : Arc() {
    override fun tailPlaceHasEnoughTokens() : Boolean {
        return (tailNode!! as Place).tokens >= multiplicity
    }

    override fun isSameArcType(other: Arc): Boolean {
        return other is NormalArc
    }

    override fun toString(): String {
        return "->"
    }
}

class VariableArc(
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

    override fun isSameArcType(other: Arc): Boolean {
        return other is VariableArc
    }

    override fun toString(): String {
        return "=>"
    }
}
