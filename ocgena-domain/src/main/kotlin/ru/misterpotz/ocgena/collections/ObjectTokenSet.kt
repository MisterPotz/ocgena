package ru.misterpotz.ocgena.collections

import ru.misterpotz.ocgena.simulation.ObjectToken
import ru.misterpotz.ocgena.simulation.ObjectTokenId

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
