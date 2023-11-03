package ru.misterpotz.ocgena.collections.objects

import kotlinx.serialization.Serializable
import model.PlaceId
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.ocnet.primitives.ext.copyWithValueTransformMutable
import java.util.SortedSet

interface PlaceToObjectMarkingDelta {
    val keys: Set<PetriAtomId>
    operator fun get(placeId: PetriAtomId): Set<ObjectTokenId>?
}

interface ImmutablePlaceToObjectMarking : ru.misterpotz.ocgena.collections.objects.PlaceToObjectMarkingDelta, java.io.Serializable {
    val tokensIterator: Iterator<ObjectTokenId>
    override operator fun get(placeId: PetriAtomId): SortedSet<ObjectTokenId>?
    override val keys: Set<PlaceId>
    fun isEmpty(): Boolean

    fun toMutable(): ru.misterpotz.ocgena.collections.objects.PlaceToObjectMarking
}

fun <S : SortedSet<ObjectTokenId>> ImmutablePlaceToObjectMarking(placesToObjectTokens: Map<PetriAtomId, S>): ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking {
    return ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarkingMap(placesToObjectTokens)
}

@Serializable
internal data class ImmutablePlaceToObjectMarkingMap(val placesToObjectTokens: Map<PetriAtomId, SortedSet<ObjectTokenId>>) :
    ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking {
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

    override fun toMutable(): ru.misterpotz.ocgena.collections.objects.PlaceToObjectMarking {
        return ru.misterpotz.ocgena.collections.objects.PlaceToObjectMarking(
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
): ru.misterpotz.ocgena.collections.objects.ObjectMarkingModifier {
    return object : ru.misterpotz.ocgena.collections.objects.ObjectMarkingModifier {
        override fun applyTo(map: MutableMap<PlaceId, SortedSet<ObjectTokenId>>) {
            block(map)
        }
    }
}