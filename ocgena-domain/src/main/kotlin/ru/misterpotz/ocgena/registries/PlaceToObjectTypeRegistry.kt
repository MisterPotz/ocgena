package ru.misterpotz.ocgena.registries

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.PlaceId
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Place

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
}
