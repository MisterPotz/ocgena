package ru.misterpotz.ocgena.simulation.interactors

import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.ocnet.utils.toObjTokenString
import ru.misterpotz.ocgena.registries.ArcsMultiplicityRegistry
import ru.misterpotz.ocgena.utils.LOG

interface ArcPrePlaceHasEnoughTokensChecker {
    fun arcInputPlaceHasEnoughTokens(
        place: PetriAtomId,
        transition: PetriAtomId,
        tokenAmountStorage: TokenAmountStorage,
    ): Boolean
}

class ArcPrePlaceHasEnoughTokensCheckerImpl(
    private val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
) : ArcPrePlaceHasEnoughTokensChecker {
    override fun arcInputPlaceHasEnoughTokens(
        place: PetriAtomId,
        transition: PetriAtomId,
        tokenAmountStorage: TokenAmountStorage,
    ): Boolean {
        val arc = place.arcIdTo(transition)
        val arcMultiplicity = arcsMultiplicityRegistry.transitionInputMultiplicityDynamic(arcId = arc)
        return arcMultiplicity.inputPlaceHasEnoughTokens(tokenAmountStorage)
    }
}

interface TokenAmountStorage {
    val places: Iterable<PetriAtomId>
    fun getTokensAt(place: PetriAtomId): Int
    fun applyDeltaTo(place: PetriAtomId, tokensDelta: Int): Int
    fun plus(tokenAmountStorage: TokenAmountStorage)
    fun minus(tokenAmountStorage: TokenAmountStorage)
    fun amountsEquals(tokenAmountStorage: TokenAmountStorage): Boolean {
        val places = places
        return places.all {
            getTokensAt(it) == tokenAmountStorage.getTokensAt(it)
        }
            .LOG { "amountEquals " }!! && (places.count() == tokenAmountStorage.places.count()).LOG { "== places iterables" }!!
    }

    fun cleanString(): String {
        return places.associateWith { getTokensAt(it) }.map {
            "${it.key} ↦ ${it.value}"
        }.joinToString(separator = "|") { it }
    }
}

data class SimpleTokenAmountStorage(
    val placeToTokens: MutableMap<PetriAtomId, Int> = mutableMapOf(),
) : TokenAmountStorage {

    override val places: List<PetriAtomId>
        get() = placeToTokens.keys.toList()

    override fun getTokensAt(place: PetriAtomId): Int {
        return placeToTokens[place]!!
    }

    override fun applyDeltaTo(place: PetriAtomId, tokensDelta: Int): Int {
        val new = (placeToTokens.getOrPut(place) {
            0
        } + tokensDelta).coerceAtLeast(0)
        placeToTokens[place] = new
        return new
    }

    override fun minus(tokenAmountStorage: TokenAmountStorage) {
        for (place in tokenAmountStorage.places) {
            val reducing = tokenAmountStorage.getTokensAt(place)
            val totalPlace = (placeToTokens.getOrElse(place) { 0 } - reducing)
            if (totalPlace < 0) {
                throw IllegalStateException("can't deduct from what doesn't exist")
            }
            placeToTokens[place] = totalPlace

            if (totalPlace == 0) {
                placeToTokens.remove(place)
            }
        }
    }

    override fun plus(tokenAmountStorage: TokenAmountStorage) {
        for (i in tokenAmountStorage.places) {
            placeToTokens[i] = placeToTokens.getOrPut(i) {
                0
            } + tokenAmountStorage.getTokensAt(i)
        }
    }

    fun reindexFrom(placeToObjectMarking: PlaceToObjectMarking) {
        placeToTokens.clear()

        for (place in placeToObjectMarking.places) {
            placeToTokens[place] = placeToObjectMarking[place].size
        }
    }
}