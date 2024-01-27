package ru.misterpotz.ocgena.simulation.stepexecutor

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.simulation.interactors.TokenAmountStorage

class ImmutableSparseTokenBunchImpl(
    val marking: ImmutablePlaceToObjectMarking,
) : SparseTokenBunch {
    override fun objectMarking(): PlaceToObjectMarking {
        return marking
    }

    override fun tokenAmountStorage(): TokenAmountStorage {
        return marking
    }

    override fun append(tokenBunch: SparseTokenBunch) {
        throw IllegalStateException()
    }

    override fun minus(tokenBunch: SparseTokenBunch) {
        throw IllegalStateException()
    }

}

fun ImmutablePlaceToObjectMarking.toImmutableBunch(): SparseTokenBunch {
    return ImmutableSparseTokenBunchImpl(this)
}