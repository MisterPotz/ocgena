package ru.misterpotz.ocgena.simulation.api.interactors

import ru.misterpotz.ocgena.ocnet.primitives.atoms.Place
import ru.misterpotz.ocgena.ocnet.primitives.ext.sortById
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import javax.inject.Inject

class RepeatabilityInteractor @Inject constructor() {
    fun sortPlaces(places : List<Place>): List<Place> {
        return places.sortById()
    }

    fun sortTokens(objectTokens: List<ObjectTokenId>) : List<ObjectTokenId> = objectTokens.sorted()
}