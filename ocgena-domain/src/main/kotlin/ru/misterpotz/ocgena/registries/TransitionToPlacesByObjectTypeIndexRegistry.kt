package ru.misterpotz.ocgena.registries

import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Place

typealias PlacesByObjectType = Map<ObjectTypeId, MutableList<Place>>

interface TransitionToPlacesByObjectTypeIndexRegistry {
    fun transitionInputPlaces(transitionId: PetriAtomId): PlacesByObjectType
    fun transitionOutputPlaces(transitionId: PetriAtomId): PlacesByObjectType
}

fun TransitionToPlacesByObjectTypeIndexRegistry(
    transitionsRegistry: TransitionsRegistry,
    placeRegistry: PlaceRegistry,
    placeToObjectTypeRegistry: PlaceToObjectTypeRegistry
): TransitionToPlacesByObjectTypeIndexRegistry {
    return TransitionToPlacesByObjectTypeIndexRegistryMap(
        transitionsRegistry = transitionsRegistry,
        placeRegistry = placeRegistry,
        placeToObjectTypeRegistry = placeToObjectTypeRegistry
    )
}

internal class TransitionToPlacesByObjectTypeIndexRegistryMap(
    private val transitionsRegistry: TransitionsRegistry,
    private val placeRegistry: PlaceRegistry,
    private val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
) : TransitionToPlacesByObjectTypeIndexRegistry {
    private val indexInputPlaces: MutableMap<PetriAtomId, PlacesByObjectType> = mutableMapOf()
    private val indexOutputPlaces: MutableMap<PetriAtomId, PlacesByObjectType> = mutableMapOf()

    override fun transitionInputPlaces(transitionId: PetriAtomId): PlacesByObjectType {
        return indexInputPlaces.getOrPut(transitionId) {
            val transition = transitionsRegistry[transitionId]

            val inputPlaces = transition.fromPlaces

            buildMap {
                for (inputPlace in inputPlaces) {
                    getOrPut(placeToObjectTypeRegistry[inputPlace]!!) { mutableListOf() }
                        .add(placeRegistry[inputPlace])
                }
            }
        }
    }

    override fun transitionOutputPlaces(transitionId: PetriAtomId): PlacesByObjectType {
        return indexOutputPlaces.getOrPut(transitionId) {
            val transition = transitionsRegistry[transitionId]

            val outputPlaces = transition.toPlaces

            buildMap {
                for (outputPlace in outputPlaces) {
                    getOrPut(placeToObjectTypeRegistry[outputPlace]!!) { mutableListOf() }
                        .add(placeRegistry[outputPlace])
                }
            }
        }
    }
}
