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
    override val inputArcs: MutableList<Arc> = mutableListOf(),
    override val outputArcs: MutableList<Arc> = mutableListOf()
) : PetriNode, LabelHolder, ConsistencyCheckable {

    override var subgraphIndex: Int = PetriAtom.UNASSIGNED_SUBGRAPH_INDEX
    var tokens : Int = 0

    override fun addInputArc(arc: Arc) {
        when (placeType) {
            PlaceType.NORMAL, PlaceType.OUTPUT, PlaceType.INPUT -> inputArcs.add(arc)
        }
    }

    override fun addOutputArc(arc: Arc) {
        when (placeType) {
            PlaceType.NORMAL, PlaceType.INPUT, PlaceType.OUTPUT -> outputArcs.add(arc)
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Place

        if (label != other.label) return false
        if (type != other.type) return false
        if (placeType != other.placeType) return false
        if (inputArcs != other.inputArcs) return false
        if (outputArcs != other.outputArcs) return false
        if (subgraphIndex != other.subgraphIndex) return false
        if (tokens != other.tokens) return false

        return true
    }

    override fun hashCode(): Int {
        var result = label?.hashCode() ?: 0
        result = 31 * result + type.hashCode()
        result = 31 * result + placeType.hashCode()
        result = 31 * result + inputArcs.hashCode()
        result = 31 * result + outputArcs.hashCode()
        result = 31 * result + subgraphIndex
        result = 31 * result + tokens
        return result
    }


}
