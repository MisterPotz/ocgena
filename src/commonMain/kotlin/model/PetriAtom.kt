package model

interface PetriAtom {
    fun acceptVisitor(visitor: PetriAtomVisitor)
    var subgraphIndex : Int

    companion object {
        const val UNASSIGNED_SUBGRAPH_INDEX = -1
    }
}
