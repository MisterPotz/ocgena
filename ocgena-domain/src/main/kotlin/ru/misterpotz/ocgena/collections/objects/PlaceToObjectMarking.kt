package ru.misterpotz.ocgena.collections.objects

import model.PlaceId
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.ocnet.primitives.ext.copyWithValueTransform
import java.util.*

interface PlaceToObjectMarking {
    val tokensIterator: Iterator<ObjectTokenId>
    operator fun get(place: PlaceId): SortedSet<ObjectTokenId>?
    operator fun set(place: PlaceId, tokens: SortedSet<ObjectTokenId>?)
    fun removePlace(placeId: PlaceId)
    fun plus(delta: ru.misterpotz.ocgena.collections.objects.PlaceToObjectMarkingDelta)
    fun minus(delta: ru.misterpotz.ocgena.collections.objects.PlaceToObjectMarkingDelta)
    fun toImmutable(): ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking
    fun modify(modifier : ru.misterpotz.ocgena.collections.objects.ObjectMarkingModifier)
    fun clear()
}

fun PlaceToObjectMarking(placesToObjectTokens: MutableMap<PlaceId, SortedSet<ObjectTokenId>> = mutableMapOf()): PlaceToObjectMarking {
    return PlaceToObjectMarkingMap(placesToObjectTokens)
}

internal class PlaceToObjectMarkingMap(val placesToObjectTokens: MutableMap<PlaceId, SortedSet<ObjectTokenId>> = mutableMapOf()) :
    PlaceToObjectMarking {
    override val tokensIterator: Iterator<ObjectTokenId>
        get() {
            return iterator {
                for (key in placesToObjectTokens.keys) {
                    yieldAll(placesToObjectTokens[key]!!)
                }
            }
        }

    override fun get(place: PlaceId): SortedSet<ObjectTokenId>? {
        return placesToObjectTokens[place]
    }

    override fun set(place: PlaceId, tokens: SortedSet<ObjectTokenId>?) {
        placesToObjectTokens.getOrElse(place) {
            mutableSetOf()
        }.addAll(tokens ?: return)
    }

    override fun removePlace(placeId: PlaceId) {
        placesToObjectTokens.remove(placeId)
    }

    override fun plus(delta: ru.misterpotz.ocgena.collections.objects.PlaceToObjectMarkingDelta) {
        val commonKeys = placesToObjectTokens.keys.intersect(delta.keys)
        for (key in commonKeys) {
            get(key)!!.addAll(delta[key]!!)
        }
    }

    override fun minus(delta: ru.misterpotz.ocgena.collections.objects.PlaceToObjectMarkingDelta) {
        val commonKeys = placesToObjectTokens.keys.intersect(delta.keys)
        for (key in commonKeys) {
            get(key)!!.removeAll(delta[key]!!)
        }
    }

    override fun toImmutable(): ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking {
        return ImmutableObjectMarking(
            placesToObjectTokens.copyWithValueTransform { it.toSortedSet() }
        )
    }

    override fun modify(modifier: ru.misterpotz.ocgena.collections.objects.ObjectMarkingModifier) {
        modifier.applyTo(placesToObjectTokens)
    }

    override fun clear() {
        placesToObjectTokens.clear()
    }
}
