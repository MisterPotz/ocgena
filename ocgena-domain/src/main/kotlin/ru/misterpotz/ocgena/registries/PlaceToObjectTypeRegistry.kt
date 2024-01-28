package ru.misterpotz.ocgena.registries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Place
import ru.misterpotz.ocgena.ocnet.utils.defaultObjTypeId

@Serializable
data class PlaceToObjectTypeRegistry(
    @SerialName("default")
    private val defaultObjectTypeId: ObjectTypeId,
    @SerialName("per_place")
    private val placeIdToObjectType: MutableMap<PetriAtomId, ObjectTypeId>
) {

    operator fun get(place: PetriAtomId): ObjectTypeId {
        return placeIdToObjectType[place] ?: defaultObjectTypeId
    }


    operator fun get(place: Place): ObjectTypeId {
        return placeIdToObjectType[place.id] ?: defaultObjectTypeId
    }

    companion object {
        fun build(block: MutableMap<PetriAtomId, ObjectTypeId>.() -> Unit): PlaceToObjectTypeRegistry {
            return PlaceToObjectTypeRegistry(
                defaultObjectTypeId = defaultObjTypeId,
                placeIdToObjectType = mutableMapOf<PetriAtomId, ObjectTypeId>().apply(block)
            )
        }
    }
}
