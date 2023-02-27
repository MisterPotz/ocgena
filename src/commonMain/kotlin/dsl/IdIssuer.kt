package dsl

class IdIssuer(private val prefix : String = "atom") {
    private var uniqueIdCounter: Long = 0L

    fun createId(): Long {
        return uniqueIdCounter++
    }

    fun decrease() {
        uniqueIdCounter--
    }

    fun createIdString(): String {
        return prefix + createId().toString()
    }
}
