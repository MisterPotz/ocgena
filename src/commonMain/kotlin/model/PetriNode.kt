package model

interface PetriNode {
    val inputArcs : List<Arc>
    val outputArcs : List<Arc>
    companion object {
        val EMPTY = emptyList<Arc>()
    }
}
