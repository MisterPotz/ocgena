package ru.misterpotz.ocgena.collections

import ru.misterpotz.ocgena.simulation.ObjectToken
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import java.util.SortedSet

interface ObjectTokenSet {
    operator fun get(objectTokenId: ObjectTokenId): ObjectToken?
    fun add(objectToken: ObjectToken)
    fun removeAll(sortedSet: SortedSet<ObjectTokenId>)
    val biggestId: ObjectTokenId?
}

internal class ObjectTokenSetMap(
    private val map: MutableMap<ObjectTokenId, ObjectToken>
) : ObjectTokenSet {
    private var _biggestId: ObjectTokenId? = null
    override val biggestId
        get() = _biggestId

    override fun get(objectTokenId: ObjectTokenId): ObjectToken? {
        return map[objectTokenId]
    }

    override fun add(objectToken: ObjectToken) {
        val biggestId = biggestId

        if (biggestId != null && objectToken.id > biggestId) {
            _biggestId = objectToken.id
        } else if (biggestId == null) {
            _biggestId = objectToken.id
        }
        map[objectToken.id] = objectToken
    }

    override fun removeAll(sortedSet: SortedSet<ObjectTokenId>) {
        for (i in sortedSet) {
            map.remove(i)
        }
    }
}
