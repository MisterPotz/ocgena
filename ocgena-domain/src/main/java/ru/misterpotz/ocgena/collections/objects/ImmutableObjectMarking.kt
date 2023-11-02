package ru.misterpotz.ocgena.collections.objects

import kotlinx.serialization.Serializable
import model.PlaceId
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.ocnet.primitives.ext.copyWithValueTransformMutable
import java.util.SortedSet

interface ObjectMarkingDelta {
    val keys : Set<PlaceId>
    operator fun get(placeId: PlaceId) : Set<ObjectTokenId>?
}

interface ImmutableObjectMarking : ObjectMarkingDelta, java.io.Serializable {
    val tokensIterator: Iterator<ObjectTokenId>
    override operator fun get(placeId: PlaceId): SortedSet<ObjectTokenId>?
    override val keys: Set<PlaceId>
    fun isEmpty(): Boolean

    fun toMutable(): PlaceToObjectMarking
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

    override fun toMutable(): PlaceToObjectMarking {
        return PlaceToObjectMarking(
            placesToObjectTokens.copyWithValueTransformMutable {
                it.toSortedSet()
            }
        )
    }
}

interface ObjectMarkingModifier {
    fun applyTo(map: MutableMap<PlaceId, SortedSet<ObjectTokenId>>)
}

fun buildObjectMarkingModifier(
    block: (modificationTarget: MutableMap<PlaceId, SortedSet<ObjectTokenId>>) -> Unit
): ObjectMarkingModifier {
    return object : ObjectMarkingModifier {
        override fun applyTo(map: MutableMap<PlaceId, SortedSet<ObjectTokenId>>) {
            block(map)
        }
    }
}