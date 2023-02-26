package model

class Transition(
    val id : String,
    override val label : String? = null,
    override val inputArcs: MutableList<Arc> = mutableListOf(),
    override val outputArcs: MutableList<Arc> = mutableListOf()
) : PetriNode, LabelHolder {
    val inputPlaces : List<Place>
        get() = inputArcs.mapNotNull { it.tailNode }.filterIsInstance<Place>()
    val outputPlaces : List<Place>
        get() = outputArcs.mapNotNull { it.arrowNode }.filterIsInstance<Place>()

    fun getEnabledBinding() : Binding? {
        return Binding.createEnabledBinding(this)
    }

    fun isBindingEnabled(): Boolean {
        return inputArcs.all { it.tailPlaceHasEnoughTokens() }
    }
}
