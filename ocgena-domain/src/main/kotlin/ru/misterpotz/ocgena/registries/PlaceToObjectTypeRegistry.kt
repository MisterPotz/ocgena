package ru.misterpotz.ocgena.registries

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.simulation.ObjectType
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.PlaceId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Place
import utils.toIds

@Serializable
data class PlaceToObjectTypeRegistry(
    private val defaultObjectTypeId: ObjectTypeId,
    private val placeIdToObjectType: MutableMap<PlaceId, ObjectTypeId>
) {

    operator fun get(place: PlaceId): ObjectTypeId {
        return placeIdToObjectType[place] ?: defaultObjectTypeId
    }


    operator fun get(place: Place): ObjectTypeId {
        return placeIdToObjectType[place.id] ?: defaultObjectTypeId
    }

    class PlaceTypingBlock {
        val mutableMap: MutableMap<ObjectTypeId, Collection<PlaceId>> = mutableMapOf()

        fun objectType(objectType: ObjectTypeId, placesIds: String) {
            mutableMap[objectType] = placesIds.toIds()
        }
    }

//    companion object {
//        fun build(
//            defaultObjectType: ObjectTypeId = "ot",
//            block: (PlaceTypingBlock.() -> Unit)? = null
//        ): PlaceToObjectTypeRegistry {
//            val placeTypingBlock = PlaceTypingBlock().apply {
//                block?.invoke(this)
//            }
//
//            val mutableMap = if (block != null) {
//                placeTypingBlock.mutableMap
//                    .toList()
//                    .fold(mutableMapOf<PlaceId, ObjectType>()) { accum, entry ->
//                        for (placeId in entry.second) {
//                            accum[placeId] = ObjectType(entry.first)
//                        }
//                        accum
//                    }
//            } else mutableMapOf()
//
//            return PlaceToObjectTypeRegistry(
//                defaultObjectTypeId = defaultObjectType,
//                placeIdToObjectType = mutableMap
//            )
//        }
//    }
}
