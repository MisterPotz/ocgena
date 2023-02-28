package dsl

class PatternIdCreator(
    private val startIndex : Int,
    private val formatter: (index: Int) -> String,
) {
    private var idIssuer: IdIssuer = IdIssuer(startIndex)

    fun newLabelId() : String {
        val index = idIssuer.createId()
        return formatter(index)
    }

    fun newIntId() : Int {
        return idIssuer.createId()
    }

    fun removeLast() {
        idIssuer.decrease()
    }

    val lastLabelId : String
        get() {
            val index = idIssuer.lastUsed
            return formatter(index)
        }

    val lastIntId : Int
        get() {
            return idIssuer.lastUsed
        }
}
