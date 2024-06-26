package ru.misterpotz.ocgena.simulation_old.stepexecutor

import ru.misterpotz.ocgena.simulation_old.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.simulation_old.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.simulation_old.interactors.TokenAmountStorage
import ru.misterpotz.ocgena.simulation_old.state.PMarkingProvider

data class GlobalSparseTokenBunch(
    private val pMarkingProvider: PMarkingProvider,
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
) : SparseTokenBunch {
    override fun objectMarking(): PlaceToObjectMarking {
        return pMarkingProvider.get()
    }

    override fun tokenAmountStorage(): TokenAmountStorage {
        return objectTokenRealAmountRegistry
    }

    override fun append(tokenBunch: SparseTokenBunch) {
        pMarkingProvider.get().plus(tokenBunch.objectMarking())
        objectTokenRealAmountRegistry.plus(tokenBunch.tokenAmountStorage())
    }

    private fun validateState() {
        if (objectTokenRealAmountRegistry.places.count() < pMarkingProvider.get().places.count()) {
            throw IllegalStateException("cannot contain more places than token storage")
        }
        for (i in objectTokenRealAmountRegistry.places) {
            if (objectTokenRealAmountRegistry.getTokensAt(i) < pMarkingProvider.get()[i].size) {
                throw IllegalStateException("cannot contain more than expected")
            }
        }
    }

    override fun minus(tokenBunch: SparseTokenBunch) {
        objectTokenRealAmountRegistry.minus(tokenBunch.tokenAmountStorage())
        pMarkingProvider.get().minus(tokenBunch.objectMarking())
        validateState()
    }

    override fun toString(): String {
        return tokenAmountStorage().places.joinToString(separator = "  ##  ") {
            "$it -- ${tokenAmountStorage().getTokensAt(it)}"
        }
    }
}