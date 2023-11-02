package model

import ru.misterpotz.model.PlaceType
import ru.misterpotz.model.atoms.Place
import ru.misterpotz.model.collections.createPetriAtomRegistry
import utils.toIds

class InputOutputPlaces(private val entries: Map<PlaceId, PlaceType>) {

    operator fun get(placeId: PlaceId): PlaceType {
        return entries[placeId] ?: PlaceType.NORMAL
    }

    fun getInputPlaces(places: Places): Places {
        return Places(
            createPetriAtomRegistry(
                places.iterable
                    .filter { get(it.id) == PlaceType.INPUT }
                    .associateBy {
                        it.id
                    }
                    .toMutableMap()
            )
        )
    }

    fun getOutputPlaces(places: Places): Places {
        return Places(
            createPetriAtomRegistry(
                places.iterable
                    .filter { get(it.id) == PlaceType.OUTPUT }
                    .associateBy {
                        it.id
                    }
                    .toMutableMap()
            )
        )
    }

    class InputOutputPlacesBlock {
        val mutableMap: MutableMap<PlaceType, Collection<PlaceId>> = mutableMapOf()

        fun inputPlaces(placesIds: Collection<PlaceId>) {
            mutableMap[PlaceType.INPUT] = placesIds
        }


        fun outputPlaces(placesIds: Collection<PlaceId>) {
            mutableMap[PlaceType.OUTPUT] = placesIds
        }

        fun inputPlaces(placesIds: String) {
            mutableMap[PlaceType.INPUT] = placesIds.toIds()
        }


        fun outputPlaces(placesIds: String) {
            mutableMap[PlaceType.OUTPUT] = placesIds.toIds()
        }
    }

    companion object {

        fun build(block: InputOutputPlacesBlock.() -> Unit): InputOutputPlaces {
            val inputOutputPlacesBlock = InputOutputPlacesBlock()
            inputOutputPlacesBlock.block()

            val mutableMap: MutableMap<PlaceType, Collection<PlaceId>> = inputOutputPlacesBlock.mutableMap

            val kek = mutableMap.toList()
                .fold<Pair<PlaceType, Collection<PlaceId>>, MutableMap<PlaceId, PlaceType>>(mutableMapOf()) { accum, entry ->
                    for (placeId in entry.second) {
                        accum[placeId] = entry.first
                    }
                    accum
                }

            return InputOutputPlaces(
                entries = kek
            )
        }
    }
}