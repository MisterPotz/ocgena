package ru.misterpotz.ocgena.ocnet.primitives

import ru.misterpotz.ocgena.simulation.binding.TokenBuffer
import ru.misterpotz.ocgena.simulation.interactors.TokenAmountStorage

interface InputArcMultiplicity {
    fun inputPlaceHasEnoughTokens(): Boolean
    fun requiredTokenAmount(): Int
}

interface InputArcMultiplicityDynamic {
    fun inputPlaceHasEnoughTokens(tokenAmountStorage: TokenAmountStorage): Boolean
    fun requiredTokenAmount(tokenAmountStorage: TokenAmountStorage): Int
}


class InputArcMultiplicityValue(
    private val sourceNodeHasEnoughTokens: Boolean,
    private val requiredTokenAmount: Int,
) : InputArcMultiplicity {
    override fun inputPlaceHasEnoughTokens(): Boolean {
        return sourceNodeHasEnoughTokens
    }

    override fun requiredTokenAmount(): Int {
        return requiredTokenAmount
    }
}

interface OutputArcMultiplicity {
    fun requiredTokenAmount(): Int
    fun getTokenSourceForThisArc() : TokenBuffer?
}

class OutputArcMultiplicityValue(
    private val requiredTokenAmount: Int,
    private val tokenBuffer: TokenBuffer?
) : OutputArcMultiplicity {

    override fun requiredTokenAmount(): Int {
        return requiredTokenAmount
    }

    override fun getTokenSourceForThisArc(): TokenBuffer? {
        return tokenBuffer
    }
}