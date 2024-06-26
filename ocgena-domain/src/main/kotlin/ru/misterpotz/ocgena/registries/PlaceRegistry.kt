package ru.misterpotz.ocgena.registries

import ru.misterpotz.ocgena.ocnet.primitives.atoms.Place
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId

interface PlaceRegistry {
    operator fun get(place: PetriAtomId): Place
    val iterable: Iterable<PetriAtomId>
    val places: Iterable<Place>
    fun isEmpty(): Boolean
    val size: Int
}

fun PlaceRegistry(petriAtomRegistry: PetriAtomRegistry): PlaceRegistry {
    return PlaceRegistryMap(petriAtomRegistry)
}

internal class PlaceRegistryMap(private val petriAtomRegistry: PetriAtomRegistry) : PlaceRegistry {

    override operator fun get(place: PetriAtomId): Place {
        return petriAtomRegistry[place] as Place
    }

    override fun isEmpty(): Boolean {
        return petriAtomRegistry.getPlaces().isEmpty()
    }

    override val size: Int
        get() = iterable.size

    override val iterable: List<PetriAtomId> =
        petriAtomRegistry.getPlaces()

    override val places: Iterable<Place> by lazy(LazyThreadSafetyMode.NONE) {
        iterable.map { petriAtomRegistry.getPlace(it) }
    }
}

