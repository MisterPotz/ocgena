package model

class Transition(
    override val label : String? = null,
    override val inputArcs: MutableList<Arc> = mutableListOf<Arc>(),
    override val outputArcs: MutableList<Arc> = mutableListOf<Arc>()
) : PetriNode, LabelHolder {


    override var subgraphIndex: Int = PetriAtom.UNASSIGNED_SUBGRAPH_INDEX

    val inputPlaces : List<Place>
        get() = inputArcs.mapNotNull { it.tailNode }.filterIsInstance<Place>()
    val outputPlaces : List<Place>
        get() = outputArcs.mapNotNull { it.arrowNode }.filterIsInstance<Place>()

    override fun addInputArc(arc: Arc) {
        inputArcs.add(arc)
    }

    override fun addOutputArc(arc: Arc) {
        outputArcs.add(arc)
    }

    fun getEnabledBinding() : Binding? {
        return Binding.createEnabledBinding(this)
    }

    fun isBindingEnabled(): Boolean {
        return inputArcs.all { it.tailPlaceHasEnoughTokens() }
    }

    override fun acceptVisitor(visitor: PetriAtomVisitorDFS) {
        visitor.visitTransition(this)
    }

    override fun isSameType(other: PetriNode): Boolean {
        return other is Transition
    }

    override fun toString(): String {
        return "transition [ $label ]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Transition

        if (label != other.label) return false
        if (inputArcs != other.inputArcs) return false
        if (outputArcs != other.outputArcs) return false
        if (subgraphIndex != other.subgraphIndex) return false
        if (inputPlaces != other.inputPlaces) return false
        if (outputPlaces != other.outputPlaces) return false

        return true
    }

    override fun hashCode(): Int {
        var result = label?.hashCode() ?: 0
        result = 31 * result + inputArcs.hashCode()
        result = 31 * result + outputArcs.hashCode()
        result = 31 * result + subgraphIndex
        result = 31 * result + inputPlaces.hashCode()
        result = 31 * result + outputPlaces.hashCode()
        return result
    }


}
