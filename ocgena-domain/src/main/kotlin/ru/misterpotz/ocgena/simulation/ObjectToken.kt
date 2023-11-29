package ru.misterpotz.ocgena.simulation

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId

interface ObjectValuesMap : java.io.Serializable

@Serializable
class EmptyObjectValuesMap : ObjectValuesMap {
    override fun toString(): String {
        return "[ ]"
    }
}

typealias Time = Long

typealias ObjectTokenId = Long

@Serializable
data class ObjectToken(
    val id: Long,
    val name: String,
    val objectTypeId: ObjectTypeId,
    val ovmap: ObjectValuesMap = EmptyObjectValuesMap(),
) {
    var ownPathTime: Time = 0
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ObjectToken

        if (id != other.id) return false
        if (name != other.name) return false
        if (objectTypeId != other.objectTypeId) return false
        if (ownPathTime != other.ownPathTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + objectTypeId.hashCode()
        result = 31 * result + ownPathTime.hashCode()
        return result
    }
}
