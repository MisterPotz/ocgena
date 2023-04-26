package model

typealias ObjectTypeId = String

data class ObjectType(
    val label : String,
    val id : ObjectTypeId = label,
) {
}
