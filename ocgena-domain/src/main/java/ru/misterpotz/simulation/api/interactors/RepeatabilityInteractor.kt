package ru.misterpotz.simulation.api.interactors

import model.Place
import model.Places
import model.sortById
import ru.misterpotz.marking.objects.ObjectToken
import ru.misterpotz.marking.objects.ObjectTokenId
import javax.inject.Inject

class RepeatabilityInteractor @Inject constructor() {
    fun sortPlaces(places : List<Place>): List<Place> {
        return places.sortById()
    }

    fun sortTokens(objectTokens: List<ObjectTokenId>) : List<ObjectTokenId> = objectTokens.sorted()
}