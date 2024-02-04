package ru.misterpotz.ocgena.collections

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.ext.copyWithValueTransform
import ru.misterpotz.ocgena.ocnet.utils.toObjTokenString
import ru.misterpotz.ocgena.simulation.ObjectToken
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.interactors.TokenAmountStorage
import java.util.*
import javax.inject.Inject

interface ObjectTokenRealAmountRegistry : TokenAmountStorage {
    fun getRealAmountAt(place: PetriAtomId): Int
    fun zeroAmountAt(place: PetriAtomId)
    fun incrementRealAmountAt(place: PetriAtomId, incrementValue: Int)
    fun decreaseRealAmountAt(place: PetriAtomId, decrementValue: Int)
    override operator fun plus(tokenAmountStorage: TokenAmountStorage)
    override operator fun minus(tokenAmountStorage: TokenAmountStorage)
}

fun ObjectTokenRealAmountRegistry(): ObjectTokenRealAmountRegistry {
    return ObjectTokenRealAmountRegistryImpl()
}

internal class ObjectTokenRealAmountRegistryImpl @Inject constructor() : ObjectTokenRealAmountRegistry {
    val placeToTokens: MutableMap<PetriAtomId, Int> = mutableMapOf()
    override val places
        get() = placeToTokens.keys

    override fun getRealAmountAt(place: PetriAtomId): Int {
        return placeToTokens.getOrPut(place) { 0 }
    }

    override fun decreaseRealAmountAt(place: PetriAtomId, decrementValue: Int) {
        placeToTokens[place] = (placeToTokens.getOrPut(place) { 0 } - decrementValue).coerceAtLeast(0)
    }

    override fun zeroAmountAt(place: PetriAtomId) {
        placeToTokens[place] = 0
    }

    override fun incrementRealAmountAt(place: PetriAtomId, incrementValue: Int) {
        placeToTokens[place] = placeToTokens.getOrPut(place) { 0 } + incrementValue
    }

    override fun plus(tokenAmountStorage: TokenAmountStorage) {
        for (place in tokenAmountStorage.places) {
            val appending = tokenAmountStorage.getTokensAt(place)
            placeToTokens[place] = placeToTokens.getOrPut(place) {
                0
            } + appending
        }
    }

    override fun minus(tokenAmountStorage: TokenAmountStorage) {
        for (place in tokenAmountStorage.places) {
            val reducing = tokenAmountStorage.getTokensAt(place)
            val totalPlace = (placeToTokens.getOrElse(place) { 0 } - reducing)
            if (totalPlace < 0) {
                throw IllegalStateException("can't deduct from what doesn't exist")
            }
            placeToTokens[place] = totalPlace
        }
    }



    override fun getTokensAt(place: PetriAtomId): Int {
        return placeToTokens[place]!!
    }

    override fun applyDeltaTo(place: PetriAtomId, tokensDelta: Int): Int {
        val new = (placeToTokens.getOrPut(place) {
            0
        } + tokensDelta).coerceAtLeast(0)
        placeToTokens[place] = new
        return new
    }

    override fun toString(): String {
        return "ObjectTokenRealAmountRegistryImpl(placeToTokens=$placeToTokens)"
    }

    companion object {
        fun build(block: MutableMap<PetriAtomId, Int>.() -> Unit): ObjectTokenRealAmountRegistryImpl {
            return mutableMapOf<PetriAtomId, Int>().apply {
                block()
            }.let {
                ObjectTokenRealAmountRegistryImpl().apply {
                    placeToTokens.putAll(it)
                }
            }
        }
    }
}

interface PlaceToObjectMarking : TokenAmountStorage {
    val tokensIterator: Iterator<ObjectTokenId>
    override val places: Iterable<PetriAtomId>
    operator fun get(place: PetriAtomId): SortedSet<ObjectTokenId>
    operator fun set(place: PetriAtomId, tokens: SortedSet<ObjectTokenId>?)
    fun add(place: PetriAtomId, objectTokenId: ObjectTokenId)
    fun removePlace(placeId: PetriAtomId)
    fun plus(delta: PlaceToObjectMarkingDelta)
    fun plus(placeToObjectMarking: PlaceToObjectMarking)
    operator fun minus(delta: PlaceToObjectMarkingDelta)
    operator fun minus(placeToObjectMarking: PlaceToObjectMarking)
    fun removeAllPlaceTokens(place: PetriAtomId)
    fun toImmutable(): ImmutablePlaceToObjectMarking
    fun modify(modifier: ObjectMarkingModifier)
    fun clear()
    fun markingEquals(placeToObjectMarking: PlaceToObjectMarking): Boolean
    override fun cleanString(): String
    fun dumpTokens() : Map<PetriAtomId, List<ObjectTokenId>> {
        return buildMap {
            for (place in places) {
                get(place)?.let {
                    put(place, it)
                }
            }
        }
    }
}

fun PlaceToObjectMarking(placesToObjectTokens: MutableMap<PetriAtomId, SortedSet<ObjectTokenId>> = mutableMapOf()): PlaceToObjectMarking {
    return PlaceToObjectMarkingMap(placesToObjectTokens)
}

@Serializable
data class PlaceToObjectMarkingMap(val placesToObjectTokens: MutableMap<PetriAtomId, SortedSet<ObjectTokenId>> = mutableMapOf()) :
    PlaceToObjectMarking {

    override val places: Iterable<PetriAtomId>
        get() = placesToObjectTokens.keys

    override val tokensIterator: Iterator<ObjectTokenId>
        get() {
            return iterator {
                for (key in placesToObjectTokens.keys) {
                    yieldAll(placesToObjectTokens[key]!!)
                }
            }
        }

    override fun removeAllPlaceTokens(place: PetriAtomId) {
        placesToObjectTokens[place]!!.clear()
    }

    override fun get(place: PetriAtomId): SortedSet<ObjectTokenId> {
        return placesToObjectTokens.getOrPut(place) {
            sortedSetOf()
        }
    }

    override fun plus(placeToObjectMarking: PlaceToObjectMarking) {
        for (i in placeToObjectMarking.places) {
            placesToObjectTokens[i] = placesToObjectTokens.getOrPut(i) {
                sortedSetOf()
            }.apply {
                val atMaskMarking = placeToObjectMarking[i]
                addAll(atMaskMarking)
            }
        }
    }

    override fun plus(tokenAmountStorage: TokenAmountStorage) {

    }

    override fun set(place: PetriAtomId, tokens: SortedSet<ObjectTokenId>?) {
        placesToObjectTokens.getOrPut(place) {
            sortedSetOf()
        }.addAll(tokens ?: return)
    }

    override fun add(place: PetriAtomId, objectTokenId: ObjectTokenId) {
        placesToObjectTokens.getOrPut(place) { sortedSetOf() }.add(objectTokenId)
    }

    override fun removePlace(placeId: PetriAtomId) {
        placesToObjectTokens.remove(placeId)
    }

    override fun plus(delta: PlaceToObjectMarkingDelta) {
        for (key in delta.keys) {
            placesToObjectTokens.getOrPut(key) {
                sortedSetOf()
            }.addAll(delta[key]!!)
        }
    }

    override fun minus(delta: PlaceToObjectMarkingDelta) {
        val commonKeys = placesToObjectTokens.keys.intersect(delta.keys)
        for (key in commonKeys) {
            get(key)!!.removeAll(delta[key]!!)
            if (get(key)!!.isEmpty()) {
                removePlace(key)
            }
        }
    }

    override fun minus(placeToObjectMarking: PlaceToObjectMarking) {
        for (i in placeToObjectMarking.places) {
            if (i in placesToObjectTokens) {
                (placesToObjectTokens[i]!!.removeAll(placeToObjectMarking[i]))
                val resulting = placesToObjectTokens[i]!!
                if (resulting.isEmpty()) {
                    placesToObjectTokens.remove(i)
                }
            }
        }
    }

    override fun minus(tokenAmountStorage: TokenAmountStorage) {
        throw IllegalStateException("not applicable")
    }

    override fun toImmutable(): ImmutablePlaceToObjectMarking {
        return ImmutablePlaceToObjectMarking(
            placesToObjectTokens.copyWithValueTransform { it.toSortedSet() }
        )
    }

    override fun modify(modifier: ObjectMarkingModifier) {
        modifier.applyTo(placesToObjectTokens)
    }

    override fun clear() {
        placesToObjectTokens.clear()
    }

    override fun markingEquals(placeToObjectMarking: PlaceToObjectMarking): Boolean {
        return placesToObjectTokens.all { it.value == placeToObjectMarking[it.key] } &&
                placesToObjectTokens.size == placeToObjectMarking.places.count()
    }

    override fun getTokensAt(place: PetriAtomId): Int {
        return get(place).size
    }

    override fun applyDeltaTo(place: PetriAtomId, tokensDelta: Int): Int {
        throw IllegalStateException("unsupported by place to object marking")
    }

    override fun cleanString(): String {
        return placesToObjectTokens.map {
            "${it.key} ↦ ${
                it.value.joinToString(
                    separator = ",",
                    prefix = "<",
                    postfix = ">"
                ) { it.toObjTokenString() }
            }"
        }.joinToString(separator = "|") { it }
    }

    companion object {
        fun build(mutableMap: MutableMap<PetriAtomId, Set<ObjectTokenId>>.() -> Unit): PlaceToObjectMarkingMap {
            return PlaceToObjectMarkingMap(mutableMapOf<PetriAtomId, Set<ObjectTokenId>>().apply { mutableMap() }
                .mapValues { it.value.toSortedSet() }
                .toMutableMap())
        }

        fun buildWithAmount(mutableMap: MutableMap<PetriAtomId, Set<ObjectTokenId>>.() -> Unit): Pair<PlaceToObjectMarkingMap, ObjectTokenRealAmountRegistry> {
            val map = mutableMapOf<PetriAtomId, Set<ObjectTokenId>>().apply { mutableMap() }
                .mapValues { it.value.toSortedSet() }
                .toMutableMap()
            val registry = ObjectTokenRealAmountRegistryImpl().apply {
                placeToTokens.apply {
                    putAll(
                        map.mapValues { it.value.size }
                    )
                }
            }

            return Pair(PlaceToObjectMarkingMap(map), registry)
        }
    }
}
