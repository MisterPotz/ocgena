package ru.misterpotz.model.marking

interface ObjectTokenSet {
    operator fun get(objectTokenId: ObjectTokenId): ObjectToken?
    fun add(objectToken: ObjectToken)
}

internal class ObjectTokenSetMap(private val map: MutableMap<ObjectTokenId, ObjectToken>) : ObjectTokenSet {
    override fun get(objectTokenId: ObjectTokenId): ObjectToken? {
        return map[objectTokenId]
    }

    override fun add(objectToken: ObjectToken) {
        map[objectToken.id] = objectToken
    }
}
