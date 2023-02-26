package model

class Place(
    override val label : String? = null,
    val type : ObjectType,
    override val inputArcs: MutableList<Arc> = mutableListOf(),
    override val outputArcs: MutableList<Arc> = mutableListOf(),
) : PetriNode, LabelHolder {
    var tokens : Int = 0

    fun consumeTokens(amount: Int) {
        require(tokens >= amount)
        tokens -= amount
    }

    fun addTokens(amount: Int) {
        tokens += amount
    }
}
