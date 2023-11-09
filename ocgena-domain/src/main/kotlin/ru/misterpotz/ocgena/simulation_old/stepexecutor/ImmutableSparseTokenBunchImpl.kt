package ru.misterpotz.ocgena.simulation_old.stepexecutor

import ru.misterpotz.ocgena.simulation_old.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.simulation_old.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.simulation_old.interactors.TokenAmountStorage

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