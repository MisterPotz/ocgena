package model

class Transition(
    val id : String,
    override val label : String? = null,
    private val _inputArcs: MutableList<Arc> = mutableListOf<Arc>(),
    private val _outputArcs: MutableList<Arc> = mutableListOf<Arc>()
) : PetriNode, LabelHolder {
    override val inputArcs: List<Arc>
        get() = _inputArcs
    override val outputArcs: List<Arc>
        get() = _outputArcs

    override var subgraphIndex: Int = PetriAtom.UNASSIGNED_SUBGRAPH_INDEX

    val inputPlaces : List<Place>
        get() = inputArcs.mapNotNull { it.tailNode }.filterIsInstance<Place>()
    val outputPlaces : List<Place>
        get() = outputArcs.mapNotNull { it.arrowNode }.filterIsInstance<Place>()

    override fun addInputArc(arc: Arc) {
        _inputArcs.add(arc)
    }

    override fun addOutputArc(arc: Arc) {
        _outputArcs.add(arc)
    }

    fun getEnabledBinding() : Binding? {
        return Binding.createEnabledBinding(this)
    }

    fun isBindingEnabled(): Boolean {
        return inputArcs.all { it.tailPlaceHasEnoughTokens() }
    }

    override fun acceptVisitor(visitor: PetriAtomVisitor) {
        visitor.visitTransition(this)
    }

    override fun isSameType(other: PetriNode): Boolean {
        return other is Transition
    }
}
