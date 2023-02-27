package model

interface PetriNode: ConsistencyCheckable, PetriAtom {
    val inputArcs : List<Arc>
    val outputArcs : List<Arc>

    fun addInputArc(arc: Arc)
    fun addOutputArc(arc: Arc)
    override fun acceptVisitor(visitor: PetriAtomVisitor)

    companion object {
        val EMPTY = emptyList<Arc>()
    }
}
