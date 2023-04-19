package model

enum class PlaceType {
    NORMAL,
    INPUT,
    OUTPUT
}

data class Place(
    override val id: String,
    override val label: String,
    val type: ObjectType,
    val placeType: PlaceType,
    override val inputArcs: MutableList<Arc> = mutableListOf(),
    override val outputArcs: MutableList<Arc> = mutableListOf(),
    override var subgraphIndex: Int = PetriAtom.UNASSIGNED_SUBGRAPH_INDEX,
) : PetriNode, LabelHolder, ConsistencyCheckable {

    var tokens: Int = 0

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

    override fun acceptVisitor(visitor: PetriAtomVisitorDFS) {
        visitor.visitPlace(this)
    }

    override fun copyWithoutConnections(): PetriNode {
        return copy(
            inputArcs = mutableListOf(),
            outputArcs = mutableListOf(),

        )
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

        if (id != other.id) return false
        if (label != other.label) return false
        if (type != other.type) return false
        if (placeType != other.placeType) return false
        if (subgraphIndex != other.subgraphIndex) return false
        if (tokens != other.tokens) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + placeType.hashCode()
        result = 31 * result + subgraphIndex
        result = 31 * result + tokens
        return result
    }
}
