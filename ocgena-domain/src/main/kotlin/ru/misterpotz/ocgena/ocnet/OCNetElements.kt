package model

import eventlog.ObjectTypes
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry

interface OCNetElements {
    val places: Places
    val transitionsRegistry: TransitionsRegistry
    val arcsRegistry: ArcsRegistry
    val objectTypes : ObjectTypes

    val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry
}

typealias PlaceId = String
