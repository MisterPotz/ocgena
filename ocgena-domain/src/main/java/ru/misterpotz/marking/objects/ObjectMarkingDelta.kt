package ru.misterpotz.marking.objects

import model.PlaceId

interface ObjectMarkingDelta {
    val keys : Set<PlaceId>
    operator fun get(placeId: PlaceId) : Set<ObjectTokenId>?
}
