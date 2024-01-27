package ru.misterpotz.ocgena.simulation.stepexecutor

import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.simulation.interactors.TokenAmountStorage
import ru.misterpotz.ocgena.utils.LOG

interface SparseTokenBunch {
    fun objectMarking(): PlaceToObjectMarking
    fun tokenAmountStorage(): TokenAmountStorage
    fun append(tokenBunch: SparseTokenBunch)
    fun minus(tokenBunch: SparseTokenBunch)
    fun bunchesEqual(tokenBunch: SparseTokenBunch): Boolean {
        return objectMarking().markingEquals(tokenBunch.objectMarking()).LOG { "marking equals" }!! &&
                tokenAmountStorage().amountsEquals(tokenBunch.tokenAmountStorage()).LOG { "token amount equals" }!!
    }

    fun cleanString(): String {
        return objectMarking().cleanString() + " ❇️ " + tokenAmountStorage().cleanString()
    }
}