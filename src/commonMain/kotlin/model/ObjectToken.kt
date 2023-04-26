package model

interface ObjectValuesMap

object EmptyObjectValuesMap : ObjectValuesMap {
    override fun toString(): String {
        return "[ ]"
    }
}

typealias Time = Int

data class ObjectToken(
    val id : Int,
    val name: String,
    val type: ObjectType,
    val ovmap: ObjectValuesMap,
) {
    var ownPathTime: Time = 0
}
