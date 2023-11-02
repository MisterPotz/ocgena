package model

import eventlog.ObjectTypes
import ru.misterpotz.ocgena.registries.PlaceObjectTypeRegistry

interface OCNetElements {
    val places: Places
    val transitionsRegistry: TransitionsRegistry
    val arcsRegistry: ArcsRegistry
    val objectTypes : ObjectTypes

    val placeObjectTypeRegistry: PlaceObjectTypeRegistry
}

typealias PlaceId = String
