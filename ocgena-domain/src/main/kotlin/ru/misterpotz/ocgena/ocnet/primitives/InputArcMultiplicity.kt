package ru.misterpotz.ocgena.ocnet.primitives

import ru.misterpotz.ocgena.simulation.typea.TokenBuffer

interface InputArcMultiplicity {
    fun inputPlaceHasEnoughTokens(): Boolean
    fun requiredTokenAmount(): Int
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
    fun bufferHasEnoughTokens(): Boolean
    fun requiredTokenAmount(): Int
    fun getTokenSourceForThisArc() : TokenBuffer
}

class OutputArcMultiplicityValue(
    private val bufferHasEnoughTokens: Boolean,
    private val requiredTokenAmount: Int,
    private val tokenBuffer: TokenBuffer
) : OutputArcMultiplicity {
    override fun bufferHasEnoughTokens(): Boolean {
        return bufferHasEnoughTokens
    }

    override fun requiredTokenAmount(): Int {
        return requiredTokenAmount
    }

    override fun getTokenSourceForThisArc(): TokenBuffer {
        return tokenBuffer
    }
}