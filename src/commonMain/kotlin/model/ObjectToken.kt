package model

interface ObjectValuesMap

object EmptyObjectValuesMap : ObjectValuesMap {
    override fun toString(): String {
        return "[ ]"
    }
}

typealias Time = Int

data class ObjectToken(
    val name: String,
    val type: ObjectType,
    val ovmap: ObjectValuesMap,
) {
    var ownPathTime: Time = 0
}
