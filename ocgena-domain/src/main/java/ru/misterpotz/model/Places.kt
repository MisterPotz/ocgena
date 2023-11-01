package model

import ru.misterpotz.model.atoms.Place

interface Places {
    operator fun get(place : PetriAtomId) : Place
}

fun Places(places: MutableMap<PetriAtomId, Place> = mutableMapOf()) : Places {
    return PlacesMap(places)
}

internal class PlacesMap(val places: MutableMap<PetriAtomId, Place> = mutableMapOf()) : Places {

    override operator fun get(place : PetriAtomId) : Place {
        return places[place]!!
    }
}

