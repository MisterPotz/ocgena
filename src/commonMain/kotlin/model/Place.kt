package model

import kotlinx.serialization.Serializable

enum class PlaceType {
    NORMAL,
    INPUT,
    OUTPUT
}

@Serializable
data class SerializablePlace(val id : String) : SerializableAtom{

}

//interface SerializableAtomFactory {
//    fun create(
//        placeTyping: PlaceTyping,
//        labelMapping: LabelMapping,
//
//    )
//}

data class Place(
    override val id: PlaceId,
    override val label: String,
//    val type: ObjectType,
    override val inputArcs: MutableList<Arc> = mutableListOf(),
    override val outputArcs: MutableList<Arc> = mutableListOf(),
    override var subgraphIndex: Int = PetriAtom.UNASSIGNED_SUBGRAPH_INDEX,
) : PetriNode, LabelHolder, ConsistencyCheckable {
    private val transitionToArc : MutableMap<Transition, Arc> = mutableMapOf()

    var tokens: Int = 0
    override val serializableAtom by lazy {
        SerializablePlace(id)
    }

    fun getArcForTransition(transition: Transition) : Arc? {
        return transitionToArc[transition]
    }

    override fun addInputArc(arc: Arc) {
        inputArcs.add(arc)
    }

    override fun addOutputArc(arc: Arc) {
        outputArcs.add(arc)
    }

    override fun acceptVisitor(visitor: PetriAtomVisitorDFS) {
        visitor.visitPlace(this)
    }

    override fun reindexArcs() {
        for (inputArc in inputArcs) {
            transitionToArc[inputArc.tailNode as Transition] = inputArc
        }
        for (outputArc in outputArcs) {
            transitionToArc[outputArc.arrowNode as Transition] = outputArc
        }
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
        return "place [$label]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Place

        if (id != other.id) return false
        if (label != other.label) return false
        if (subgraphIndex != other.subgraphIndex) return false
        if (tokens != other.tokens) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + subgraphIndex
        result = 31 * result + tokens
        return result
    }
}
