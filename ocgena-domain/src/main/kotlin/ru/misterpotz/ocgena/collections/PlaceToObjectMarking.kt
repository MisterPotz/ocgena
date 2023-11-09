package ru.misterpotz.ocgena.collections

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.ext.copyWithValueTransform
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import java.util.*

interface ObjectTokenRealAmountRegistry {
    fun getRealAmountAt(place: PetriAtomId): Int
    fun setRealAmountAt(place: PetriAtomId, newRealAmount: Int)
    fun incrementRealAmountAt(place: PetriAtomId, incrementValue: Int)
    fun getRealAmountOfType(objectTypeId: ObjectTypeId): Int
    fun incrementRealAmountOfType(objectTypeId: ObjectTypeId, incrementValue: Int)
}

fun ObjectTokenRealAmountRegistry(placeToObjectTypeRegistry: PlaceToObjectTypeRegistry): ObjectTokenRealAmountRegistry {
    return ObjectTokenRealAmountRegistryImpl(placeToObjectTypeRegistry)
}

internal class ObjectTokenRealAmountRegistryImpl(
    private val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry
) : ObjectTokenRealAmountRegistry {
    private val placeToAmount = mutableMapOf<PetriAtomId, Int>()
    private val typeToAmount = mutableMapOf<ObjectTypeId, Int>()
    override fun getRealAmountAt(place: PetriAtomId): Int {
        return placeToAmount[place]!!
    }

    override fun setRealAmountAt(place: PetriAtomId, newRealAmount: Int) {
        val placeOldTokenAmount = placeToAmount[place]
        placeToAmount[place] = newRealAmount
//        val objectTypeId = placeToObjectTypeRegistry[place]
//        val newTypeAmount = typeToAmount[objectTypeId]!! - placeOldTokenAmount!! + newRealAmount
//
//        typeToAmount[objectTypeId] = newTypeAmount
    }

    override fun incrementRealAmountAt(place: PetriAtomId, incrementValue: Int) {
        placeToAmount[place] = placeToAmount.getOrPut(place) { 0 } + incrementValue
    }

    override fun getRealAmountOfType(objectTypeId: ObjectTypeId): Int {
        return typeToAmount[objectTypeId] ?: 0
    }

    override fun incrementRealAmountOfType(objectTypeId: ObjectTypeId, incrementValue: Int) {
        typeToAmount[objectTypeId] = typeToAmount.getOrPut(objectTypeId) { 0 } + incrementValue
    }
}

interface PlaceToObjectMarking {
    val tokensIterator: Iterator<ObjectTokenId>
    operator fun get(place: PetriAtomId): SortedSet<ObjectTokenId>?
    operator fun set(place: PetriAtomId, tokens: SortedSet<ObjectTokenId>?)
    fun getRealTokenAmount(place: PetriAtomId): Int?
    fun setRealTokenAmount(place: PetriAtomId, newRealAmount: Int?)
    fun removePlace(placeId: PetriAtomId)
    fun plus(delta: PlaceToObjectMarkingDelta)
    fun minus(delta: PlaceToObjectMarkingDelta)
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

    override fun get(place: PetriAtomId): SortedSet<ObjectTokenId>? {
        return placesToObjectTokens[place]
    }

    override fun set(place: PetriAtomId, tokens: SortedSet<ObjectTokenId>?) {
        placesToObjectTokens.getOrPut(place) {
            sortedSetOf()
        }.addAll(tokens ?: return)
    }

    override fun getRealTokenAmount(place: PetriAtomId): Int? {
        return placeToRealTokenAmount[place]
    }

    override fun setRealTokenAmount(place: PetriAtomId, newRealAmount: Int?) {
        if (newRealAmount == null) {
            placeToRealTokenAmount.remove(place)
        } else {
            placeToRealTokenAmount[place] = newRealAmount
        }
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
}
