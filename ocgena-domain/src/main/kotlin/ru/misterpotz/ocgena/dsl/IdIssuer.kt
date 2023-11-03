package ru.misterpotz.ocgena.dsl

class IdIssuer(private val startIndex : Long = 0) {
    private var uniqueIdCounter: Long = startIndex

    val lastUsed: Long
        get() {
            require(uniqueIdCounter > startIndex)
            return uniqueIdCounter - 1
        }

    fun createId(): Long {
        return uniqueIdCounter++
    }

    fun decrease() {
        uniqueIdCounter--
    }
}

