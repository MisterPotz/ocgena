package ru.misterpotz.model

import model.ObjectTokenId
import model.PlaceId

interface ObjectMarkingDelta {
    val keys : Set<PlaceId>
    operator fun get(placeId: PlaceId) : Set<ObjectTokenId>?
}
