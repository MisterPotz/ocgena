package model

class Place(
    override val label : String? = null,
    val type : ObjectType,
    override val inputArcs: List<Arc> = emptyList(),
    override val outputArcs: List<Arc> = emptyList(),
) : PetriNode, LabelHolder {
    var tokens : Int = 0

    fun consume(amount: Int) {
        require
    }
}
