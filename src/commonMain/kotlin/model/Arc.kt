package model

abstract class Arc {
    open var arrowNode : PetriNode? = null
    open var tailNode : PetriNode? = null

    fun requireTailPlace() : Place {
        return checkNotNull(tailNode as? Place) {
            "tail place was required not null"
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
}
