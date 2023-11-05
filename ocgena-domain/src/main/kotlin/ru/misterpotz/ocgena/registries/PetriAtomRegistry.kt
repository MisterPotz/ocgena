package ru.misterpotz.ocgena.registries

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtom
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Place
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo

interface PetriAtomRegistry {
    operator fun get(petriAtomId: PetriAtomId): PetriAtom
    fun getPlace(petriAtomId: PetriAtomId): Place
    fun getTransition(petriAtomId: PetriAtomId): Transition
    fun getArc(petriAtomId: PetriAtomId): Arc
    fun getPlaces(): Iterable<PetriAtomId>
    fun getTransitions(): Iterable<PetriAtomId>
    fun getArcs(): Iterable<PetriAtomId>
    fun PetriAtomId.asNode(): PetriAtom
    var PetriAtomId.subgraphIndex: Int?

    fun PetriAtomId.arcTo(petriAtom: PetriAtomId): Arc
    operator fun set(petriAtomId: PetriAtomId, petriAtom: PetriAtom)
    val iterator: MutableIterator<PetriAtomId>
}

fun PetriAtomRegistry(elements: Map<PetriAtomId, PetriAtom>): PetriAtomRegistry {
    return PetriAtomRegistryImpl(elements.toMutableMap())
}

@Serializable
internal data class PetriAtomRegistryImpl(
    private val map: MutableMap<PetriAtomId, PetriAtom> = mutableMapOf(),
) : PetriAtomRegistry {
    private val subgraphIndexData: MutableMap<PetriAtomId, Int?> = mutableMapOf()
    override fun get(petriAtomId: PetriAtomId): PetriAtom {
        return requireNotNull(map[petriAtomId]) {
            "petri atom wasn't found for id $petriAtomId"
        }
    }

    override fun PetriAtomId.asNode(): PetriAtom {
        return get(this)
    }

    override var PetriAtomId.subgraphIndex: Int?
        get() = subgraphIndexData[this]
        set(value) {
            subgraphIndexData[this] = value
        }

    override fun getPlace(petriAtomId: PetriAtomId): Place {
        return get(petriAtomId) as Place
    }

    override fun getTransition(petriAtomId: PetriAtomId): Transition {
        return get(petriAtomId) as Transition
    }

    override fun getArc(petriAtomId: PetriAtomId): Arc {
        return get(petriAtomId) as Arc
    }

    override fun getPlaces(): Iterable<PetriAtomId> {
        return map.keys.asSequence().filter { get(it) is Place }.asIterable()
    }

    override fun getTransitions(): Iterable<PetriAtomId> {
        return map.keys.asSequence().filter { get(it) is Transition }.asIterable()
    }

    override fun getArcs(): Iterable<PetriAtomId> {
        return map.keys.asSequence().filter { get(it) is Arc }.asIterable()
    }

    override fun set(petriAtomId: PetriAtomId, petriAtom: PetriAtom) {
        map[petriAtomId] = petriAtom
    }

    override fun PetriAtomId.arcTo(petriAtom: PetriAtomId): Arc {
        return getArc(arcIdTo(petriAtom))
    }

    override val iterator: MutableIterator<PetriAtomId>
        get() = map.keys.iterator()
}
