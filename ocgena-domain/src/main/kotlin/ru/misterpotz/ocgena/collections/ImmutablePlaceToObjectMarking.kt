package ru.misterpotz.ocgena.collections

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.ocnet.primitives.ext.copyWithValueTransformMutable
import ru.misterpotz.ocgena.simulation.interactors.TokenAmountStorage
import java.util.SortedSet

interface PlaceToObjectMarkingDelta {
    val keys: Set<PetriAtomId>
    operator fun get(placeId: PetriAtomId): Set<ObjectTokenId>?
}

interface ImmutablePlaceToObjectMarking : PlaceToObjectMarkingDelta, java.io.Serializable, TokenAmountStorage {
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
@SerialName("placeToObject")
data class ImmutablePlaceToObjectMarkingMap(@SerialName("per_place") val placesToObjectTokens: Map<PetriAtomId, SortedSet<ObjectTokenId>>) :
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

    override fun getTokensAt(place: PetriAtomId): Int {
        return get(place).size
    }

    override fun applyDeltaTo(place: PetriAtomId, tokensDelta: Int): Int {
        throw IllegalStateException("cannot modify immutable place to object marking")
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