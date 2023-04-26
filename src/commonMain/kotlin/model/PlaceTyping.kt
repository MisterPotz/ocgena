package model

import eventlog.ObjectTypes

class InputOutputPlaces(private val entries: Map<PlaceId, PlaceType>) {

    operator fun get(placeId : PlaceId) : PlaceType {
        return entries[placeId] ?: PlaceType.NORMAL
    }


    operator fun get(placeId : Place) : PlaceType {
        return entries[placeId.id] ?: PlaceType.NORMAL
    }

    fun getInputPlaces(places: Places) : Places {
        return Places(places.filter { get(it.id) == PlaceType.INPUT })
    }

    fun getOutputPlaces(places: Places) : Places {
        return Places(places.filter { get(it.id) == PlaceType.OUTPUT })
    }

    companion object {
        fun build(block: MutableMap<PlaceType, Collection<PlaceId>>.() -> Unit): InputOutputPlaces {
            val mutableMap: MutableMap<PlaceType, Collection<PlaceId>> =
                mutableMapOf<PlaceType, Collection<PlaceId>>().apply(block)

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

class PlaceTyping(
    private val defaultObjectType: ObjectType,
    private val placeIdToObjectType: MutableMap<PlaceId, ObjectType>
) {

    fun allObjectTypes() : Set<ObjectType> {
        return placeIdToObjectType.values.toSet()
    }
    operator fun get(place: PlaceId): ObjectType {
        return placeIdToObjectType[place] ?: defaultObjectType
    }


    operator fun get(place: Place): ObjectType {
        return placeIdToObjectType[place.id] ?: defaultObjectType
    }

    companion object {
        fun build(
            defaultObjectType: ObjectTypeId = "ot",
            block: (MutableMap<ObjectTypeId, Collection<PlaceId>>.() -> Unit)? = null
        ): PlaceTyping {
            val mutableMap = if (block != null) {
                mutableMapOf<ObjectTypeId, Collection<PlaceId>>().apply(block)
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
