package ru.misterpotz.ocgena.registries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.PlaceType
import ru.misterpotz.ocgena.utils.toIds

@Serializable
data class PlaceTypeRegistry(@SerialName("per_place") private val entries: Map<PetriAtomId, PlaceType>) {

    operator fun get(placeId: PetriAtomId): PlaceType {
        return entries[placeId] ?: PlaceType.NORMAL
    }

    fun getInputPlaces(placeRegistry: PlaceRegistry): PlaceRegistry {
        return PlaceRegistry(
            PetriAtomRegistry(
                placeRegistry.iterable
                    .filter { get(it) == PlaceType.INPUT }
                    .map {
                        placeRegistry[it]
                    }
                    .associateBy {
                        it.id
                    }
                    .toMutableMap()
            )
        )
    }

    fun getOutputPlaces(placeRegistry: PlaceRegistry): PlaceRegistry {
        return PlaceRegistry(
            PetriAtomRegistry(
                placeRegistry.iterable
                    .filter { get(it) == PlaceType.OUTPUT }
                    .map { placeRegistry[it] }
                    .associateBy {
                        it.id
                    }
                    .toMutableMap()
            )
        )
    }

    class InputOutputPlacesBlock {
        val mutableMap: MutableMap<PlaceType, Collection<PetriAtomId>> = mutableMapOf()

        fun inputPlaces(placesIds: Collection<PetriAtomId>) {
            mutableMap[PlaceType.INPUT] = placesIds
        }


        fun outputPlaces(placesIds: Collection<PetriAtomId>) {
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

        fun build(block: InputOutputPlacesBlock.() -> Unit): PlaceTypeRegistry {
            val inputOutputPlacesBlock = InputOutputPlacesBlock()
            inputOutputPlacesBlock.block()

            val mutableMap: MutableMap<PlaceType, Collection<PetriAtomId>> = inputOutputPlacesBlock.mutableMap

            val kek = mutableMap.toList()
                .fold<Pair<PlaceType, Collection<PetriAtomId>>, MutableMap<PetriAtomId, PlaceType>>(mutableMapOf()) { accum, entry ->
                    for (placeId in entry.second) {
                        accum[placeId] = entry.first
                    }
                    accum
                }

            return PlaceTypeRegistry(
                entries = kek
            )
        }
    }
}