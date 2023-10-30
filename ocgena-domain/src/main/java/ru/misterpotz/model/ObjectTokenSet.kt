package ru.misterpotz.model

import model.ObjectToken
import model.ObjectTokenId

interface ObjectTokenSet {
    operator fun get(objectTokenId: ObjectTokenId): ObjectToken?
    operator fun set(objectTokenId: ObjectTokenId, objectToken: ObjectToken)
}

internal class ObjectTokenSetMap(private val map: MutableMap<ObjectTokenId, ObjectToken>) : ObjectTokenSet {
    override fun get(objectTokenId: ObjectTokenId): ObjectToken? {
        return map[objectTokenId]
    }

    override fun set(objectTokenId: ObjectTokenId, objectToken: ObjectToken) {
        map[objectTokenId] = objectToken
    }
}
