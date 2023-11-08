package ru.misterpotz.ocgena.collections

import ru.misterpotz.ocgena.simulation.ObjectToken
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import java.util.SortedSet

interface ObjectTokenSet {
    operator fun get(objectTokenId: ObjectTokenId): ObjectToken?
    fun add(objectToken: ObjectToken)
    fun removeAll(sortedSet: SortedSet<ObjectTokenId>)
}

internal class ObjectTokenSetMap(private val map: MutableMap<ObjectTokenId, ObjectToken>) : ObjectTokenSet {
    override fun get(objectTokenId: ObjectTokenId): ObjectToken? {
        return map[objectTokenId]
    }

    override fun add(objectToken: ObjectToken) {
        map[objectToken.id] = objectToken
    }

    override fun removeAll(sortedSet: SortedSet<ObjectTokenId>) {
        for (i in sortedSet) {
            map.remove(i)
        }
    }
}
