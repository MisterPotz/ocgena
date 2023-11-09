package ru.misterpotz.ocgena.collections

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.ocnet.primitives.ext.copyWithValueTransformMutable
import java.util.SortedSet

interface PlaceToObjectMarkingDelta {
    val keys: Set<PetriAtomId>
    operator fun get(placeId: PetriAtomId): Set<ObjectTokenId>?
}

interface ImmutablePlaceToObjectMarking : PlaceToObjectMarkingDelta, java.io.Serializable {
    val tokensIterator: Iterator<ObjectTokenId>
    override operator fun get(placeId: PetriAtomId): SortedSet<ObjectTokenId>
    override val keys: Set<PetriAtomId>
    fun isEmpty(): Boolean

    fun toMutable(): PlaceToObjectMarking
}

fun <S : SortedSet<ObjectTokenId>> ImmutablePlaceToObjectMarking(placesToObjectTokens: Map<PetriAtomId, S>): ImmutablePlaceToObjectMarking {
    return ImmutablePlaceToObjectMarkingMap(placesToObjectTokens)
}

@Serializable
internal data class ImmutablePlaceToObjectMarkingMap(val placesToObjectTokens: Map<PetriAtomId, SortedSet<ObjectTokenId>>) :
    ImmutablePlaceToObjectMarking {
    override val tokensIterator: Iterator<ObjectTokenId>
        get() {
            return iterator {
                for (key in placesToObjectTokens.keys) {
                    yieldAll(placesToObjectTokens[key]!!)
                }
            }
        }

    override fun get(placeId: PetriAtomId): SortedSet<ObjectTokenId> {
        return placesToObjectTokens[placeId] ?: sortedSetOf()
    }

    override val keys: Set<PetriAtomId> by lazy(LazyThreadSafetyMode.NONE) {
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
    fun applyTo(map: MutableMap<PetriAtomId, SortedSet<ObjectTokenId>>)
}

fun buildObjectMarkingModifier(
    block: (modificationTarget: MutableMap<PetriAtomId, SortedSet<ObjectTokenId>>) -> Unit
): ObjectMarkingModifier {
    return object : ObjectMarkingModifier {
        override fun applyTo(map: MutableMap<PetriAtomId, SortedSet<ObjectTokenId>>) {
            block(map)
        }
    }
}