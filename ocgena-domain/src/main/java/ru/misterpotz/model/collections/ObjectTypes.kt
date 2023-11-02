package ru.misterpotz.model.collections

import kotlinx.serialization.Serializable
import model.ObjectType

@Serializable
data class ObjectTypes(private val objectTYpes: List<ObjectType>) : List<ObjectType> by objectTYpes
