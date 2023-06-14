package model

import kotlinx.serialization.Serializable
import utils.toIds

class InputOutputPlaces(private val entries: Map<PlaceId, PlaceType>) {

    operator fun get(placeId: PlaceId): PlaceType {
        return entries[placeId] ?: PlaceType.NORMAL
    }


    operator fun get(placeId: Place): PlaceType {
        return entries[placeId.id] ?: PlaceType.NORMAL
    }

    fun getInputPlaces(places: Places): Places {
        return Places(places.filter { get(it.id) == PlaceType.INPUT })
    }

    fun getOutputPlaces(places: Places): Places {
        return Places(places.filter { get(it.id) == PlaceType.OUTPUT })
    }

    class InputOutputPlacesBlock {
        val mutableMap : MutableMap<PlaceType, Collection<PlaceId>> = mutableMapOf()

//        fun inputPlaces(vararg placesIds: PlaceId) {
//           mutableMap[PlaceType.INPUT] = placesIds.toList()
//        }

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
//
//        fun outputPlaces(vararg placesIds: PlaceId) {
//            mutableMap[PlaceType.OUTPUT] = placesIds.toList()
//        }
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

@Serializable
data class SerializablePlaceTyping(
    val defaultObjectType: ObjectType,
    val placeIdToObjectType: MutableMap<PlaceId, ObjectType>
)

class PlaceTyping(
    private val defaultObjectType: ObjectType,
    private val placeIdToObjectType: MutableMap<PlaceId, ObjectType>
) {

    fun toSerializable() :SerializablePlaceTyping {
        return SerializablePlaceTyping(
            defaultObjectType = defaultObjectType,
            placeIdToObjectType = placeIdToObjectType
        )
    }

    fun toObjectTypes() : ObjectTypes {
        return ObjectTypes(allObjectTypes().toList())
    }
    fun allObjectTypes(): Set<ObjectType> {
        return placeIdToObjectType.values.toMutableSet().apply {
            if (isEmpty()) add(defaultObjectType)
        }
    }

    operator fun get(place: PlaceId): ObjectType {
        return placeIdToObjectType[place] ?: defaultObjectType
    }


    operator fun get(place: Place): ObjectType {
        return placeIdToObjectType[place.id] ?: defaultObjectType
    }


    class PlaceTypingBlock {
        val mutableMap : MutableMap<ObjectTypeId, Collection<PlaceId>> = mutableMapOf()

        fun objectType(objectType: ObjectTypeId, placesIds:String) {
            mutableMap[objectType] = placesIds.toIds()
        }
    }

    companion object {
        fun build(
            defaultObjectType: ObjectTypeId = "ot",
            block: (PlaceTypingBlock.() -> Unit)? = null
        ): PlaceTyping {
            val placeTypingBlock = PlaceTypingBlock().apply {
                block?.invoke(this)
            }

            val mutableMap = if (block != null) {
                placeTypingBlock.mutableMap
                    .toList()
                    .fold(mutableMapOf<PlaceId, ObjectType>()) { accum, entry ->
                        for (placeId in entry.second) {
                            accum[placeId] = ObjectType(entry.first)
                        }
                        accum
                    }
            } else mutableMapOf()

            return PlaceTyping(
                defaultObjectType = ObjectType(defaultObjectType),
                placeIdToObjectType = mutableMap
            )
        }
    }
}
