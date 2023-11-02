package ru.misterpotz.marking.objects

import ru.misterpotz.model.ext.copyWithValueTransform
import model.PlaceId
import java.util.SortedSet

interface ObjectMarking {
    val tokensIterator: Iterator<ObjectTokenId>
    operator fun get(place: PlaceId): SortedSet<ObjectTokenId>?
    operator fun set(place: PlaceId, tokens: SortedSet<ObjectTokenId>?)
    fun removePlace(placeId: PlaceId)
    fun plus(delta: ObjectMarkingDelta)
    fun minus(delta: ObjectMarkingDelta)
    fun toImmutable(): ImmutableObjectMarking
    fun modify(modifier : ObjectMarkingModifier)
    fun clear()
}

internal class ObjectMarkingMap(val placesToObjectTokens: MutableMap<PlaceId, SortedSet<ObjectTokenId>> = mutableMapOf()) :
    ObjectMarking {
    override val tokensIterator: Iterator<ObjectTokenId>
        get() {
            return iterator {
                for (key in placesToObjectTokens.keys) {
                    yieldAll(placesToObjectTokens[key]!!)
                }
            }
        }

    override fun get(place: PlaceId): SortedSet<ObjectTokenId>? {
        return placesToObjectTokens[place]
    }

    override fun set(place: PlaceId, tokens: SortedSet<ObjectTokenId>?) {
        placesToObjectTokens.getOrElse(place) {
            mutableSetOf()
        }.addAll(tokens ?: return)
    }

    override fun removePlace(placeId: PlaceId) {
        placesToObjectTokens.remove(placeId)
    }

    override fun plus(delta: ObjectMarkingDelta) {
        val commonKeys = placesToObjectTokens.keys.intersect(delta.keys)
        for (key in commonKeys) {
            get(key)!!.addAll(delta[key]!!)
        }
    }

    override fun minus(delta: ObjectMarkingDelta) {
        val commonKeys = placesToObjectTokens.keys.intersect(delta.keys)
        for (key in commonKeys) {
            get(key)!!.removeAll(delta[key]!!)
        }
    }

    override fun toImmutable(): ImmutableObjectMarking {
        return ImmutableObjectMarking(
            placesToObjectTokens.copyWithValueTransform { it.toSortedSet() }
        )
    }

    override fun modify(modifier: ObjectMarkingModifier) {
        modifier.applyTo(placesToObjectTokens)
    }

    override fun clear() {
        placesToObjectTokens.clear()
    }
}

fun ObjectMarking(placesToObjectTokens: MutableMap<PlaceId, SortedSet<ObjectTokenId>> = mutableMapOf()): ObjectMarking {
    return ObjectMarkingMap(placesToObjectTokens)
}