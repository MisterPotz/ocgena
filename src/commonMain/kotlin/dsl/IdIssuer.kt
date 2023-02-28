package dsl

class IdIssuer(private val startIndex : Int = 0) {
    private var uniqueIdCounter: Int = startIndex

    val lastUsed: Int
        get() {
            require(uniqueIdCounter > startIndex)
            return uniqueIdCounter - 1
        }

    fun createId(): Int {
        return uniqueIdCounter++
    }

    fun decrease() {
        uniqueIdCounter--
    }
}

