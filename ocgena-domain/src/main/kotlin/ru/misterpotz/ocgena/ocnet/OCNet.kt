package ru.misterpotz.ocgena.ocnet

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.registries.*

interface OCNet {
    val objectTypeRegistry: ObjectTypeRegistry
    val placeTypeRegistry: PlaceTypeRegistry
    val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry
    val placeRegistry: PlaceRegistry
    val transitionsRegistry: TransitionsRegistry
    val arcsRegistry: ArcsRegistry
    val petriAtomRegistry: PetriAtomRegistry

    val inputPlaces: PlaceRegistry
    val outputPlaces: PlaceRegistry
}

@Deprecated("use petriatomid")
typealias PlaceId = String

@Serializable
data class OCNetImpl(
    override val objectTypeRegistry: ObjectTypeRegistry,
    override val placeTypeRegistry: PlaceTypeRegistry,
    override val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
    override val petriAtomRegistry: PetriAtomRegistry
) : OCNet {
    override val placeRegistry: PlaceRegistry by lazy(LazyThreadSafetyMode.NONE) {
        PlaceRegistry(petriAtomRegistry)
    }
    override val transitionsRegistry: TransitionsRegistry by lazy(LazyThreadSafetyMode.NONE) {
        TransitionsRegistry(petriAtomRegistry)
    }
    override val arcsRegistry: ArcsRegistry by lazy(LazyThreadSafetyMode.NONE) {
        ArcsRegistry(petriAtomRegistry)
    }
    override val inputPlaces: PlaceRegistry by lazy(LazyThreadSafetyMode.NONE) {
        placeTypeRegistry.getInputPlaces(placeRegistry)
    }
    override val outputPlaces: PlaceRegistry by lazy(LazyThreadSafetyMode.NONE) {
        placeTypeRegistry.getOutputPlaces(placeRegistry)
    }
}