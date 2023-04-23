package model

interface PetriNode: ConsistencyCheckable, PetriAtom {
    val inputArcs : MutableList<Arc>
    val outputArcs : MutableList<Arc>
    val label : String

    fun addInputArc(arc: Arc)
    fun addOutputArc(arc: Arc)

    fun isSameType(other : PetriNode) : Boolean
    override fun acceptVisitor(visitor: PetriAtomVisitorDFS)

    fun reindexArcs()

    fun copyWithoutConnections() : PetriNode
    companion object {
        val EMPTY = emptyList<Arc>()
    }
}
