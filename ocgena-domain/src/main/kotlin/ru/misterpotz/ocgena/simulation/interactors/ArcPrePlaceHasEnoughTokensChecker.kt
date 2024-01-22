package ru.misterpotz.ocgena.simulation.interactors

import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.registries.ArcsMultiplicityRegistry

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
    val places : List<PetriAtomId>
    fun getTokensAt(place: PetriAtomId): Int
    fun applyDeltaTo(place: PetriAtomId, tokensDelta: Int): Int
    fun plus(tokenAmountStorage: TokenAmountStorage)
}

class SimpleTokenAmountStorage(
    private val placeToTokens: MutableMap<PetriAtomId, Int> = mutableMapOf(),
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