package ru.misterpotz.ocgena.simulation_v2.entities

import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.ObjectType
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunch
import ru.misterpotz.ocgena.simulation_v2.entities_storage.SortedTokens
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice
import ru.misterpotz.ocgena.simulation_v2.utils.Identifiable

class PlaceWrapper(
    val placeId: PetriAtomId,
    val objectType: ObjectType,
) : Identifiable, Comparable<PlaceWrapper> {
    override val id: String
        get() = placeId

    override fun compareTo(other: PlaceWrapper): Int {
        return placeId.compareTo(other.placeId)
    }

    fun getTokensAmount(tokenBunch: SparseTokenBunch): Int {
        return tokenBunch.tokenAmountStorage().getTokensAt(placeId)
    }

    fun tokenRangeAt(tokenSlice: TokenSlice): IntRange {
        return tokenSlice.tokensAt(this).indices
    }

    fun getTokens(tokenSlice: TokenSlice): SortedTokens {
        return tokenSlice.tokensAt(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaceWrapper

        if (placeId != other.placeId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = placeId.hashCode()
        return result
    }

    override fun toString(): String {
        return "PlaceWrapper($placeId)"
    }
}