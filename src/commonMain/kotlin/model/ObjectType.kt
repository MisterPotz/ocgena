package model

import kotlinx.serialization.Serializable

typealias ObjectTypeId = String

@Serializable
data class ObjectType(
    val label : String,
    val id : ObjectTypeId = label,
) {
}
