package ru.misterpotz.model.collections

import model.PetriAtom
import model.PetriAtomId
import ru.misterpotz.model.atoms.Arc
import ru.misterpotz.model.atoms.Place
import ru.misterpotz.model.atoms.Transition
import ru.misterpotz.model.ext.arcIdTo

class PetriAtomRegistryImpl(
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
        get() = getSubgraphIndex(this)
        set(value) = setSubgraphIndex(this, value)

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
        return map.keys.asSequence().filter { get(it) is Transition }.asIterable()
    }
    override fun set(petriAtomId: PetriAtomId, petriAtom: PetriAtom) {
        map[petriAtomId] = petriAtom
    }

    override fun PetriAtomId.arcTo(petriAtom: PetriAtomId): Arc {
        return getArc(arcIdTo(petriAtom))
    }

    override fun getSubgraphIndex(petriAtomId: PetriAtomId): Int? {
        return subgraphIndexData[petriAtomId]
    }

    override fun setSubgraphIndex(petriAtomId: PetriAtomId, subgraphIndex : Int?) {
        subgraphIndexData[petriAtomId] = subgraphIndex
    }
    override val iterator: MutableIterator<PetriAtomId>
        get() = map.keys.iterator()
}
