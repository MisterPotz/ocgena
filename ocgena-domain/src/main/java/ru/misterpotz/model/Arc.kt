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

    abstract fun isSameArcType(other: Arc): Boolean

    abstract fun copyWithTailAndArrow(
        newTail: PetriNode,
        newArrow: PetriNode,
    ): Arc
}

