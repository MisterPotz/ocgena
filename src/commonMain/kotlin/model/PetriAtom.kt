package model

interface PetriAtom {
    fun acceptConsistencyChecker(visitor: ParsingConsistencyCheckVisitor)
    var subgraphIndex : Int

    companion object {
        const val UNASSIGNED_SUBGRAPH_INDEX = -1
    }
}
