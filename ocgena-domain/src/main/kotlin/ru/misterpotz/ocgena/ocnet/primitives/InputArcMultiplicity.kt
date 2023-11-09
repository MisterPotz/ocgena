package ru.misterpotz.ocgena.ocnet.primitives

import ru.misterpotz.ocgena.simulation_old.binding.TokenSet
import ru.misterpotz.ocgena.simulation_old.binding.buffer.TokenGroupedInfo
import ru.misterpotz.ocgena.simulation_old.interactors.TokenAmountStorage

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
    fun getTokenSourceForThisArc() : TokenSet?
}

interface OutputArcMultiplicityDynamic {
    fun requiredTokenAmount(tokenGroupedInfo: TokenGroupedInfo): Int
    fun getTokenSourceForThisArc(tokenGroupedInfo: TokenGroupedInfo) : TokenSet?
}

class OutputArcMultiplicityValue(
    private val requiredTokenAmount: Int,
    private val tokenSet: TokenSet?
) : OutputArcMultiplicity {

    override fun requiredTokenAmount(): Int {
        return requiredTokenAmount
    }

    override fun getTokenSourceForThisArc(): TokenSet? {
        return tokenSet
    }
}