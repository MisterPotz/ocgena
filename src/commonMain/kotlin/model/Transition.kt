package model

class Transition(
    val id : String,
    override val label : String? = null,
    override val inputArcs: List<Arc> = emptyList(),
    override val outputArcs: List<Arc> = emptyList()
) : PetriNode, LabelHolder {
    val inputPlaces : List<Place> = inputArcs.mapNotNull { it.tailNode }.filterIsInstance<Place>()
    val outputPlaces : List<Place> = outputArcs.mapNotNull { it.arrowNode }.filterIsInstance<Place>()

    fun getEnabledBinding() : Binding? {
        return Binding.createEnabledBinding(this)
    }

    fun isBindingEnabled(): Boolean {
        return inputArcs.all { it.tailPlaceHasEnoughTokens() }
    }
}
