package ru.misterpotz.ocgena.simulation.interactors

import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import javax.inject.Inject

class RepeatabilityInteractor @Inject constructor() {
    fun sortPlaces(places : List<PetriAtomId>): List<PetriAtomId> {
        return places.sorted()
    }

    fun sortTokens(objectTokens: List<ObjectTokenId>) : List<ObjectTokenId> = objectTokens.sorted()
}