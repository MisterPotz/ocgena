package ru.misterpotz.ocgena.ocnet

import kotlinx.serialization.SerialName
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

@Serializable
data class OCNetStruct(
    @SerialName("object_types")
    override val objectTypeRegistry: ObjectTypeRegistryMap,
    @SerialName("place_types")
    override val placeTypeRegistry: PlaceTypeRegistry,
    @SerialName("place_object_types")
    override val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
    @SerialName("petri_atoms")
    override val petriAtomRegistry: PetriAtomRegistryStruct
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