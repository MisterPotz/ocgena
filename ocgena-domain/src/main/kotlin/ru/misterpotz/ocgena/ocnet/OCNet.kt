package ru.misterpotz.ocgena.ocnet

import kotlinx.serialization.Serializable
import model.ArcsRegistry
import ru.misterpotz.ocgena.eventlog.ObjectTypes
import ru.misterpotz.ocgena.registries.*

interface OCNet {
    val objectTypes: ObjectTypes
    val placeTypeRegistry: PlaceTypeRegistry
    val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry
    val placeRegistry: PlaceRegistry
    val transitionsRegistry: TransitionsRegistry
    val arcsRegistry: ArcsRegistry
    val petriAtomRegistry: PetriAtomRegistry

    val inputPlaces : PlaceRegistry
    val outputPlaces : PlaceRegistry
}

typealias PlaceId = String

@Serializable
data class OCNetImpl(
    override val objectTypes: ObjectTypes,
    override val placeTypeRegistry: PlaceTypeRegistry,
    override val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
    override val petriAtomRegistry: PetriAtomRegistry
) : OCNet {
    override val placeRegistry: PlaceRegistry by lazy {
        PlaceRegistry(petriAtomRegistry)
    }
    override val transitionsRegistry: TransitionsRegistry by lazy {
        TransitionsRegistry(petriAtomRegistry)
    }
    override val arcsRegistry: ArcsRegistry by lazy {
        ArcsRegistry(petriAtomRegistry)
    }
    override val inputPlaces: PlaceRegistry by lazy {
        placeTypeRegistry.getInputPlaces(placeRegistry)
    }
    override val outputPlaces: PlaceRegistry by lazy {
        placeTypeRegistry.getOutputPlaces(placeRegistry)
    }
}