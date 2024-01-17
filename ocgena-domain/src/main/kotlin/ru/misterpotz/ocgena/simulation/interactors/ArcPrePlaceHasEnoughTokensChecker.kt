package ru.misterpotz.ocgena.simulation.interactors

import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.registries.ArcsMultiplicityRegistry

interface ArcPrePlaceHasEnoughTokensChecker {
    fun arcInputPlaceHasEnoughTokens(
        place: PetriAtomId,
        transition: PetriAtomId,
        tokenAmountStorage: TokenAmountStorage
    ): Boolean
}

class ArcPrePlaceHasEnoughTokensCheckerImpl(
    private val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
) : ArcPrePlaceHasEnoughTokensChecker {
    override fun arcInputPlaceHasEnoughTokens(
        place: PetriAtomId,
        transition: PetriAtomId,
        tokenAmountStorage: TokenAmountStorage
    ): Boolean {
        val arc = place.arcIdTo(transition)
        val arcMultiplicity = arcsMultiplicityRegistry.transitionInputMultiplicityDynamic(arcId = arc)
        return arcMultiplicity.inputPlaceHasEnoughTokens(tokenAmountStorage)
    }
}

interface TokenAmountStorage {
    fun getTokensAt(place: PetriAtomId) : Int
}