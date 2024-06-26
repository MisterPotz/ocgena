package ru.misterpotz.ocgena.registries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.simulation_old.ObjectType

interface ObjectTypeRegistry {
    val types: List<ObjectType>
    val size: Int
    operator fun get(objectTypeId: ObjectTypeId): ObjectType
}

fun ObjectTypeRegistry(objectTypes: MutableMap<ObjectTypeId, ObjectType>): ObjectTypeRegistry {
    return ObjectTypeRegistryMap(objectTypes)
}

@Serializable
data class ObjectTypeRegistryMap(@SerialName("per_object_type_id") private val objectTypes: MutableMap<ObjectTypeId, ObjectType>) :
    ObjectTypeRegistry {
    override val types: List<ObjectType>
        get() = objectTypes.values.toList()
    override val size: Int
        get() = objectTypes.values.size

    override fun get(objectTypeId: ObjectTypeId): ObjectType {
        return objectTypes[objectTypeId]!!
    }
}

