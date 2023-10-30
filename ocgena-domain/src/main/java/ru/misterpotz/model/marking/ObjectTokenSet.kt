package ru.misterpotz.model.marking

interface ObjectTokenSet {
    fun add(objectToken: ObjectToken)
}

internal class ObjectTokenSetMap(private val map: MutableMap<ObjectTokenId, ObjectToken>) : ObjectTokenSet {
    override fun add(objectToken: ObjectToken) {
        map[objectToken.id] = objectToken
    }
}
