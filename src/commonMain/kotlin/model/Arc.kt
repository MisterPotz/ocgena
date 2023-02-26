package model

abstract class Arc {
    open val arrowNode : PetriNode? = null
    open val tailNode : PetriNode? = null
    abstract val multiplicity : Int

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
    override val arrowNode: PetriNode?,
    override val tailNode: PetriNode?,
    override val multiplicity: Int = 1
) : Arc() {
    override fun tailPlaceHasEnoughTokens() : Boolean {
        return (tailNode!! as Place).tokens >= multiplicity
    }

    override fun isSameArcType(other: Arc): Boolean {
        return other is NormalArc
    }
}

class VariableArc(
    override val arrowNode: PetriNode?,
    override val tailNode: PetriNode?,
    // TODO: sets up the allowed multiplicity dynamically, probably needs some parameters
    private val _multiplicity : () -> Int,
) : Arc() {
    override val multiplicity : Int
        get() = _multiplicity()
    override fun tailPlaceHasEnoughTokens(): Boolean {
        val multiplicity = _multiplicity()
        return (tailNode!! as Place).tokens >= multiplicity
    }

    override fun isSameArcType(other: Arc): Boolean {
        return other is VariableArc
    }
}
