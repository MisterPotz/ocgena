package model

import ru.misterpotz.model.atoms.Place
import ru.misterpotz.model.collections.PetriAtomRegistry

interface Places {
    operator fun get(place : PetriAtomId) : Place
    val iterable: Iterable<Place>
    fun isEmpty() : Boolean
}

fun Places(petriAtomRegistry: PetriAtomRegistry) : Places {
    return PlacesMap(petriAtomRegistry)
}

internal class PlacesMap(private val petriAtomRegistry: PetriAtomRegistry) : Places {

    override operator fun get(place : PetriAtomId) : Place {
        return petriAtomRegistry[place] as Place
    }

    override fun isEmpty(): Boolean {
        return petriAtomRegistry.getPlaces().toList().isEmpty()
    }

    override val iterable: Iterable<Place> = petriAtomRegistry
        .getPlaces()
        .map { petriAtomRegistry.getPlace(it) }
}

