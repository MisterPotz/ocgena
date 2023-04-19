package model

data class Transition(
    override val id: String,
    override val label : String,
    override val inputArcs: MutableList<Arc> = mutableListOf<Arc>(),
    override val outputArcs: MutableList<Arc> = mutableListOf<Arc>(),
    override var subgraphIndex: Int = PetriAtom.UNASSIGNED_SUBGRAPH_INDEX
) : PetriNode, LabelHolder {

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

    fun getEnabledBinding() : ActiveBinding? {
        return ActiveBinding.createEnabledBinding(this)
    }

    fun isBindingEnabled(): Boolean {
        return inputArcs.all { it.tailPlaceHasEnoughTokens() }
    }

    override fun acceptVisitor(visitor: PetriAtomVisitorDFS) {
        visitor.visitTransition(this)
    }

    override fun copyWithoutConnections(): PetriNode {
        return copy(
            inputArcs = mutableListOf(),
            outputArcs = mutableListOf()
        )
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

        if (id != other.id) return false
        if (label != other.label) return false
        if (subgraphIndex != other.subgraphIndex) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + subgraphIndex
        return result
    }
}
