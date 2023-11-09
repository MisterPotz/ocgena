package ru.misterpotz.ocgena.simulation_old.stepexecutor

import ru.misterpotz.ocgena.simulation_old.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.simulation_old.collections.PlaceToObjectMarkingMap
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation_old.interactors.SimpleTokenAmountStorage
import ru.misterpotz.ocgena.simulation_old.interactors.TokenAmountStorage
import ru.misterpotz.ocgena.utils.LOG

interface SparseTokenBunch {
    fun objectMarking(): PlaceToObjectMarking
    fun tokenAmountStorage(): TokenAmountStorage
    fun append(tokenBunch: SparseTokenBunch)
    fun minus(tokenBunch: SparseTokenBunch)
    fun bunchesExactlyEqual(tokenBunch: SparseTokenBunch): Boolean {
        return objectMarking().markingEquals(tokenBunch.objectMarking()).LOG { "marking equals" }!! &&
                tokenAmountStorage().projectAmountsEqual(tokenBunch.tokenAmountStorage()).LOG { "token amount equals" }!!
    }

    fun narrowTo(places: Iterable<PetriAtomId>): SparseTokenBunch {
        return SparseTokenBunchImpl(
            marking = PlaceToObjectMarkingMap.build {
                for (place in places) {
                    put(place, objectMarking()[place])
                }
            },
            tokenAmountStorage = SimpleTokenAmountStorage.build {
                for (place in places) {
                    put(place, tokenAmountStorage().getTokensAt(place))
                }
            }
        )
    }

    fun projectBunchAmountsEqual(tokenBunch: SparseTokenBunch): Boolean {
        return tokenAmountStorage().projectAmountsEqual(tokenBunch.tokenAmountStorage())
    }

    fun cleanString(): String {
        return objectMarking().cleanString() + " ❇️ " + tokenAmountStorage().cleanString()
    }
}