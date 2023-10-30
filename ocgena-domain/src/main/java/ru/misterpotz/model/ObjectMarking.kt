package ru.misterpotz.model

import ext.copyWithValueTransform
import model.ObjectTokenId
import model.PlaceId

interface ObjectMarking {
    val tokensIterator: Iterator<ObjectTokenId>
    operator fun get(place: PlaceId): MutableSet<ObjectTokenId>?
    operator fun set(place: PlaceId, tokens: Set<ObjectTokenId>?)
    fun removePlace(placeId: PlaceId)
    fun plus(delta: ObjectMarkingDelta)
    fun minus(delta: ObjectMarkingDelta)
    fun toImmutable(): ImmutableObjectMarking
}

internal class ObjectMarkingMap(val placesToObjectTokens: MutableMap<PlaceId, MutableSet<ObjectTokenId>> = mutableMapOf()) :
    ObjectMarking {
    override val tokensIterator: Iterator<ObjectTokenId>
        get() {
            return iterator {
                for (key in placesToObjectTokens.keys) {
                    yieldAll(placesToObjectTokens[key]!!)
                }
            }
        }

    override fun get(place: PlaceId): MutableSet<ObjectTokenId>? {
        return placesToObjectTokens[place]
    }

    override fun set(place: PlaceId, tokens: Set<ObjectTokenId>?) {
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
            placesToObjectTokens.copyWithValueTransform { it.toSet() }
        )
    }

}

fun ObjectMarking(placesToObjectTokens: MutableMap<PlaceId, MutableSet<ObjectTokenId>> = mutableMapOf()): ObjectMarking {
    return ObjectMarkingMap(placesToObjectTokens)
}