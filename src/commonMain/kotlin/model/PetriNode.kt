package model

interface PetriNode: ConsistencyCheckable, PetriAtom {
    val inputArcs : MutableList<Arc>
    val outputArcs : MutableList<Arc>

    fun addInputArc(arc: Arc)
    fun addOutputArc(arc: Arc)

    fun isSameType(other : PetriNode) : Boolean
    override fun acceptVisitor(visitor: PetriAtomVisitor)

    companion object {
        val EMPTY = emptyList<Arc>()
    }
}
