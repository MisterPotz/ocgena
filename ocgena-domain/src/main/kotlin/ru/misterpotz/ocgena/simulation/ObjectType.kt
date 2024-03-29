package ru.misterpotz.ocgena.simulation

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId

@Serializable
data class ObjectType(
    val label : String,
    val id : ObjectTypeId = label,
) {
}