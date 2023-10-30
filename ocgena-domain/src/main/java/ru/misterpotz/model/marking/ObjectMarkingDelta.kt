package ru.misterpotz.model.marking

import model.PlaceId

interface ObjectMarkingDelta {
    val keys : Set<PlaceId>
    operator fun get(placeId: PlaceId) : Set<ObjectTokenId>?
}
