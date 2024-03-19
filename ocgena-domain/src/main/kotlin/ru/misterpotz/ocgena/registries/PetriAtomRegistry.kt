package ru.misterpotz.ocgena.registries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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
    fun getPlaces(): List<PetriAtomId>
    fun getTransitions(): List<PetriAtomId>
    fun getArcs(): List<PetriAtomId>
    fun PetriAtomId.asNode(): PetriAtom
    var PetriAtomId.subgraphIndex: Int?

    fun PetriAtomId.arcTo(petriAtom: PetriAtomId): Arc
    operator fun set(petriAtomId: PetriAtomId, petriAtom: PetriAtom)
    val iterator: MutableIterator<PetriAtomId>
}

fun PetriAtomRegistry(elements: Map<PetriAtomId, PetriAtom>): PetriAtomRegistry {
    return PetriAtomRegistryStruct(elements.toMutableMap())
}

@Serializable
@SerialName("atoms")
data class PetriAtomRegistryStruct(
    @SerialName("per_id")
    val map: MutableMap<PetriAtomId, PetriAtom> = mutableMapOf(),
) : PetriAtomRegistry {
    @Transient
    private val subgraphIndexData: MutableMap<PetriAtomId, Int?> = mutableMapOf()

    private val _places by lazy(LazyThreadSafetyMode.NONE) {
        map.keys.asSequence().filter { get(it) is Place }.sortedBy { it }.toList()
    }
    private val _transitions by lazy(LazyThreadSafetyMode.NONE) {
        map.keys.asSequence().filter { get(it) is Transition }.sortedBy { it }.toList()
    }
    private val _arcs by lazy(LazyThreadSafetyMode.NONE) {
        map.keys.asSequence().filter { get(it) is Arc }.sortedBy { it }.toList()
    }
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

    override fun getPlaces(): List<PetriAtomId> {
        return _places
    }

    override fun getTransitions(): List<PetriAtomId> {
        return _transitions
    }

    override fun getArcs(): List<PetriAtomId> {
        return _arcs
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
