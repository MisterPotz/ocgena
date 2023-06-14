package model

interface PetriAtom {
    val id : String
    fun acceptVisitor(visitor: PetriAtomVisitorDFS)
    var subgraphIndex : Int
    val serializableAtom: SerializableAtom

    companion object {
        const val UNASSIGNED_SUBGRAPH_INDEX = -1
    }
}
