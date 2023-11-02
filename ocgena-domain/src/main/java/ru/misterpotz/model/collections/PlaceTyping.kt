package model

import kotlinx.serialization.Serializable
import ru.misterpotz.model.atoms.Place
import ru.misterpotz.model.collections.ObjectTypes
import utils.toIds

@Serializable
data class PlaceTyping(
    private val defaultObjectType: ObjectType,
    private val placeIdToObjectType: MutableMap<PlaceId, ObjectType>
) {
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
