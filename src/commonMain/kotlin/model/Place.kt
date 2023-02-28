package model

enum class PlaceType {
    NORMAL,
    INPUT,
    OUTPUT
}
class Place(
    override val label : String? = null,
    val type : ObjectType,
    val placeType : PlaceType,
    val _inputArcs: MutableList<Arc> = mutableListOf(),
    val _outputArcs: MutableList<Arc> = mutableListOf()
) : PetriNode, LabelHolder, ConsistencyCheckable {
    override val inputArcs: List<Arc>
        get() = _inputArcs
    override val outputArcs: List<Arc>
        get() = _outputArcs

    override var subgraphIndex: Int = PetriAtom.UNASSIGNED_SUBGRAPH_INDEX
    var tokens : Int = 0

    override fun addInputArc(arc: Arc) {
        when (placeType) {
            PlaceType.NORMAL, PlaceType.OUTPUT, PlaceType.INPUT -> _inputArcs.add(arc)
        }
    }

    override fun addOutputArc(arc: Arc) {
        when (placeType) {
            PlaceType.NORMAL, PlaceType.INPUT, PlaceType.OUTPUT -> _outputArcs.add(arc)
        }
    }

    override fun acceptVisitor(visitor: PetriAtomVisitor) {
        visitor.visitPlace(this)
    }

    fun consumeTokens(amount: Int) {
        require(tokens >= amount)
        tokens -= amount
    }

    fun addTokens(amount: Int) {
        tokens += amount
    }

    override fun isSameType(other: PetriNode): Boolean {
        return other is Place
    }

    override fun toString(): String {
        return "place [$label [ $type ]]"
    }
}
