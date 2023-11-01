package ru.misterpotz.marking.objects

import kotlinx.serialization.Serializable
import ru.misterpotz.ext.copyWithValueTransformMutable
import model.PlaceId
import java.util.SortedSet

interface ImmutableObjectMarking : ObjectMarkingDelta, java.io.Serializable {
    val tokensIterator: Iterator<ObjectTokenId>
    override operator fun get(placeId: PlaceId): SortedSet<ObjectTokenId>?
    override val keys: Set<PlaceId>
    fun isEmpty() : Boolean

    fun toMutable(): ObjectMarking
}

fun <S : SortedSet<ObjectTokenId>> ImmutableObjectMarking(placesToObjectTokens: Map<PlaceId, S>): ImmutableObjectMarking {
    return ImmutableObjectMarkingMap(placesToObjectTokens)
}

@Serializable
internal data class ImmutableObjectMarkingMap(val placesToObjectTokens: Map<PlaceId, SortedSet<ObjectTokenId>>) :
    ImmutableObjectMarking {
    override val tokensIterator: Iterator<ObjectTokenId>
        get() {
            return iterator {
                for (key in placesToObjectTokens.keys) {
                    yieldAll(placesToObjectTokens[key]!!)
                }
            }
        }

    override fun get(placeId: PlaceId): SortedSet<ObjectTokenId>? {
        return placesToObjectTokens[placeId]
    }

    override val keys: Set<PlaceId> by lazy(LazyThreadSafetyMode.NONE) {
        placesToObjectTokens.keys
    }

    override fun isEmpty(): Boolean {
        return placesToObjectTokens.isEmpty() || placesToObjectTokens.all {
            it.value.isEmpty()
        }
    }

    override fun toMutable(): ObjectMarking {
        return ObjectMarking(
            placesToObjectTokens.copyWithValueTransformMutable {
                it.toSortedSet()
            }
        )
    }
}