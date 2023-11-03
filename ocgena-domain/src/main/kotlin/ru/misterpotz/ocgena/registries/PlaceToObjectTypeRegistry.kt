package ru.misterpotz.ocgena.registries

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.simulation.ObjectType
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import model.PlaceId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Place
import utils.toIds

@Serializable
data class PlaceToObjectTypeRegistry(
    private val defaultObjectType: ObjectType,
    private val placeIdToObjectType: MutableMap<PlaceId, ObjectType>
) {
    fun toObjectTypes() : ObjectTypeRegistry {
        return ObjectTypeRegistry(allObjectTypes().associateBy { it.id }.toMutableMap())
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
        ): PlaceToObjectTypeRegistry {
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

            return PlaceToObjectTypeRegistry(
                defaultObjectType = ObjectType(defaultObjectType),
                placeIdToObjectType = mutableMap
            )
        }
    }
}
