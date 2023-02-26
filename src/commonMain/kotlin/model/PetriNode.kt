package model

interface PetriNode {
    val inputArcs : MutableList<Arc>
    val outputArcs : MutableList<Arc>
    companion object {
        val EMPTY = emptyList<Arc>()
    }
}
