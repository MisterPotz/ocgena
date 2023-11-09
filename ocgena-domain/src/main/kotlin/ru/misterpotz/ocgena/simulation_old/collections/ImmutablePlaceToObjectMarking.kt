package ru.misterpotz.ocgena.simulation_old.collections

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation_old.ObjectTokenId
import ru.misterpotz.ocgena.ocnet.primitives.ext.copyWithValueTransformMutable
import ru.misterpotz.ocgena.ocnet.utils.toObjTokenString
import ru.misterpotz.ocgena.simulation_old.interactors.TokenAmountStorage
import java.util.SortedSet

interface PlaceToObjectMarkingDelta {
    val keys: Set<PetriAtomId>
    operator fun get(placeId: PetriAtomId): Set<ObjectTokenId>?
}

interface ImmutablePlaceToObjectMarking : PlaceToObjectMarkingDelta,
    java.io.Serializable,
    TokenAmountStorage,
    PlaceToObjectMarking {

    override val tokensIterator: Iterator<ObjectTokenId>
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
    override val places by lazy(LazyThreadSafetyMode.NONE) {
        placesToObjectTokens.keys
    }
    override val tokensIterator: Iterator<ObjectTokenId>
        get() {
            return iterator {
                for (key in placesToObjectTokens.keys) {
                    yieldAll(placesToObjectTokens[key]!!)
                }
            }
        }

    override fun cleanString(): String {
        return placesToObjectTokens.map {
            "${it.key} ↦ ${it.value.joinToString(separator = ",", prefix = "<", postfix = ">") { it.toObjTokenString() }}"
        }.joinToString(separator = "|") { it }
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

    override fun plus(tokenAmountStorage: TokenAmountStorage) {
        throw IllegalStateException("canno modify immutable place to object marking")
    }

    override fun plus(delta: PlaceToObjectMarkingDelta) {
        throw IllegalStateException()
    }

    override fun plus(placeToObjectMarking: PlaceToObjectMarking) {
        throw IllegalStateException()
    }

    override fun minus(tokenAmountStorage: TokenAmountStorage) {
        throw IllegalStateException("not applicable")
    }

    override fun set(place: PetriAtomId, tokens: SortedSet<ObjectTokenId>?) {
        throw IllegalStateException()
    }

    override fun add(place: PetriAtomId, objectTokenId: ObjectTokenId) {
        throw IllegalStateException()
    }

    override fun removePlace(placeId: PetriAtomId) {
        throw IllegalStateException()
    }

    override fun minus(delta: PlaceToObjectMarkingDelta) {
        throw IllegalStateException()
    }

    override fun minus(placeToObjectMarking: PlaceToObjectMarking) {
        throw IllegalStateException()
    }

    override fun removeAllPlaceTokens(place: PetriAtomId) {
        throw IllegalStateException()
    }

    override fun toImmutable(): ImmutablePlaceToObjectMarking {
        return this
    }

    override fun modify(modifier: ObjectMarkingModifier) {
        throw IllegalStateException()
    }

    override fun clear() {
        throw IllegalStateException()
    }

    override fun markingEquals(placeToObjectMarking: PlaceToObjectMarking): Boolean {
        return placesToObjectTokens.all { it.value == placeToObjectMarking[it.key] } &&
                placesToObjectTokens.keys.size == placeToObjectMarking.places.count()
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
