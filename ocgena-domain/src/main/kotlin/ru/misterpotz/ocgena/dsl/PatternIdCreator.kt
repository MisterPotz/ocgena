package ru.misterpotz.ocgena.dsl

class PatternIdCreator(
    private val startIndex : Long,
    private val formatter: (index: Long) -> String,
) {
    private var idIssuer: IdIssuer = IdIssuer(startIndex)

    fun newLabelId() : String {
        val index = idIssuer.createId()
        return formatter(index)
    }

    fun newIntId() : Long {
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

    val lastIntId : Long
        get() {
            return idIssuer.lastUsed
        }
}
