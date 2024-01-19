package ru.misterpotz.ocgena.simulation.interactors

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
    fun getTokensAt(place: PetriAtomId): Int
    fun applyDeltaTo(place: PetriAtomId, tokensDelta: Int): Int
}

class SimpleTokenAmountStorage(
    private val placeToTokens: MutableMap<PetriAtomId, Int> = mutableMapOf(),
) : TokenAmountStorage {
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

}