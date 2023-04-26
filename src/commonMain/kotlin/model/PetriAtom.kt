package model

interface PetriAtom {
    val id : String
    fun acceptVisitor(visitor: PetriAtomVisitorDFS)
    var subgraphIndex : Int

    companion object {
        const val UNASSIGNED_SUBGRAPH_INDEX = -1
    }
}
