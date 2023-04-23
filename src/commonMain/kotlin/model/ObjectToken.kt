package model

interface ObjectValuesMap

typealias Time = Int

data class ObjectToken(
    val name: String,
    val type: ObjectType,
    val ovmap: ObjectValuesMap,
    val lastUpdateTime: Time
)
