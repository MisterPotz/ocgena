package ru.misterpotz.ocgena.ocnet.primitives

interface ArcMultiplicity {
    fun inputPlaceHasEnoughTokens(): Boolean
    fun requiredTokenAmount() : Int
}

class ArcMultiplicityValue(
    private val inputPlaceHasEnoughTokens : Boolean,
    private val requiredTokenAmount : Int,
) : ArcMultiplicity {
    override fun inputPlaceHasEnoughTokens(): Boolean {
        return inputPlaceHasEnoughTokens
    }

    override fun requiredTokenAmount(): Int {
        return requiredTokenAmount
    }
}
