package ru.misterpotz.ocgena.collections

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.ext.copyWithValueTransform
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.interactors.TokenAmountStorage
import java.util.*
import javax.inject.Inject

interface ObjectTokenRealAmountRegistry : TokenAmountStorage {
    fun getRealAmountAt(place: PetriAtomId): Int
    fun zeroAmountAt(place: PetriAtomId)
    fun incrementRealAmountAt(place: PetriAtomId, incrementValue: Int)
    fun decreaseRealAmountAt(place: PetriAtomId, decrementValue: Int)
    fun getRealAmountOfType(objectTypeId: ObjectTypeId): Int
    fun incrementRealAmountOfType(objectTypeId: ObjectTypeId, incrementValue: Int)
    fun decrementRealAmountOfType(objectTypeId: ObjectTypeId, decrementValue: Int)
}

fun ObjectTokenRealAmountRegistry(placeToObjectTypeRegistry: PlaceToObjectTypeRegistry): ObjectTokenRealAmountRegistry {
    return ObjectTokenRealAmountRegistryImpl(placeToObjectTypeRegistry)
}

internal class ObjectTokenRealAmountRegistryImpl @Inject constructor(
    private val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry
) : ObjectTokenRealAmountRegistry {
    private val placeToAmount = mutableMapOf<PetriAtomId, Int>()
    private val typeToAmount = mutableMapOf<ObjectTypeId, Int>()
    override fun getRealAmountAt(place: PetriAtomId): Int {
        return placeToAmount.getOrPut(place) { 0 }
    }

    override fun decreaseRealAmountAt(place: PetriAtomId, decrementValue: Int) {
        placeToAmount[place] = (placeToAmount.getOrPut(place) { 0 } - decrementValue).coerceAtLeast(0)
        decrementRealAmountOfType(placeToObjectTypeRegistry[place], decrementValue)
    }

    override fun zeroAmountAt(place: PetriAtomId) {
        placeToAmount[place] = 0
    }

//    override fun setRealAmountAt(place: PetriAtomId, newRealAmount: Int) {
//        val placeOldTokenAmount = placeToAmount[place]
//        placeToAmount[place] = newRealAmount
//        val objectTypeId = placeToObjectTypeRegistry[place]
//
//        val newTypeAmount = typeToAmount[objectTypeId]!! - placeOldTokenAmount!! + newRealAmount
////
////        typeToAmount[objectTypeId] = newTypeAmount
//    }

    override fun incrementRealAmountAt(place: PetriAtomId, incrementValue: Int) {
        placeToAmount[place] = placeToAmount.getOrPut(place) { 0 } + incrementValue
        val objectTypeId = placeToObjectTypeRegistry[place]
        incrementRealAmountOfType(objectTypeId, incrementValue)
    }

    override fun getRealAmountOfType(objectTypeId: ObjectTypeId): Int {
        return typeToAmount[objectTypeId] ?: 0
    }

    override fun incrementRealAmountOfType(objectTypeId: ObjectTypeId, incrementValue: Int) {
        typeToAmount[objectTypeId] = typeToAmount.getOrPut(objectTypeId) { 0 } + incrementValue
    }

    override fun decrementRealAmountOfType(objectTypeId: ObjectTypeId, decrementValue: Int) {
        typeToAmount[objectTypeId] = (typeToAmount.getOrPut(objectTypeId) { 0 } - decrementValue).coerceAtLeast(0)
    }

    override fun getTokensAt(place: PetriAtomId): Int {
        return placeToAmount[place]!!
    }

    override fun applyDeltaTo(place: PetriAtomId, tokensDelta: Int): Int {
        val new = (placeToAmount.getOrPut(place) {
            0
        } + tokensDelta).coerceAtLeast(0)
        placeToAmount[place] = new
        return new
    }
}

interface PlaceToObjectMarking: TokenAmountStorage {
    val tokensIterator: Iterator<ObjectTokenId>
    val places : Iterable<PetriAtomId>
    operator fun get(place: PetriAtomId): SortedSet<ObjectTokenId>
    operator fun set(place: PetriAtomId, tokens: SortedSet<ObjectTokenId>?)
    fun add(place: PetriAtomId, objectTokenId: ObjectTokenId)
    fun removePlace(placeId: PetriAtomId)
    fun plus(delta: PlaceToObjectMarkingDelta)
    fun plus(placeToObjectMarking: PlaceToObjectMarking)
    operator fun minus(delta: PlaceToObjectMarkingDelta)
    fun removeAllPlaceTokens(place: PetriAtomId)
    fun toImmutable(): ImmutablePlaceToObjectMarking
    fun modify(modifier: ObjectMarkingModifier)
    fun clear()
}

fun PlaceToObjectMarking(placesToObjectTokens: MutableMap<PetriAtomId, SortedSet<ObjectTokenId>> = mutableMapOf()): PlaceToObjectMarking {
    return PlaceToObjectMarkingMap(placesToObjectTokens)
}

@Serializable
data class PlaceToObjectMarkingMap(val placesToObjectTokens: MutableMap<PetriAtomId, SortedSet<ObjectTokenId>> = mutableMapOf()) :
    PlaceToObjectMarking {
    override val places: Iterable<PetriAtomId>
        get() = placesToObjectTokens.keys

    @Transient
    private val placeToRealTokenAmount: MutableMap<PetriAtomId, Int> = mutableMapOf()

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

    override fun getTokensAt(place: PetriAtomId): Int {
        return get(place).size
    }

    override fun applyDeltaTo(place: PetriAtomId, tokensDelta: Int): Int {
        throw IllegalStateException("unsupported by place to object marking")
    }
}
