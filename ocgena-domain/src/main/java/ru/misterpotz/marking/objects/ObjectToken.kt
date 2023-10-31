package ru.misterpotz.marking.objects

import kotlinx.serialization.Serializable
import model.ObjectType

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
    val type: ObjectType,
    val ovmap: ObjectValuesMap = EmptyObjectValuesMap(),
) {
    var ownPathTime: Time = 0
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ObjectToken

        if (id != other.id) return false
        if (name != other.name) return false
        return type == other.type
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}
