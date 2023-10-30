package ru.misterpotz.model

import ext.copyWithValueTransformMutable
import model.ObjectTokenId
import model.PlaceId

interface ImmutableObjectMarking : ObjectMarkingDelta {
    val tokensIterator: Iterator<ObjectTokenId>
    override operator fun get(placeId: PlaceId): Set<ObjectTokenId>?
    override val keys: Set<PlaceId>

    fun toMutable(): ObjectMarking
}

fun ImmutableObjectMarking(placesToObjectTokens: Map<PlaceId, Set<ObjectTokenId>>): ImmutableObjectMarking {
    return ImmutableObjectMarkingMap(placesToObjectTokens)
}

internal class ImmutableObjectMarkingMap(val placesToObjectTokens: Map<PlaceId, Set<ObjectTokenId>>) :
    ImmutableObjectMarking {
    override val tokensIterator: Iterator<ObjectTokenId>
        get() {
            return iterator {
                for (key in placesToObjectTokens.keys) {
                    yieldAll(placesToObjectTokens[key]!!)
                }
            }
        }

    override fun get(placeId: PlaceId): Set<ObjectTokenId>? {
        return placesToObjectTokens[placeId]
    }

    override val keys: Set<PlaceId> by lazy(LazyThreadSafetyMode.NONE) {
        placesToObjectTokens.keys
    }

    override fun toMutable(): ObjectMarking {
        return ObjectMarking(
            placesToObjectTokens.copyWithValueTransformMutable {
                it.toMutableSet()
            }
        )
    }
}