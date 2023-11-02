package ru.misterpotz.model.collections

import model.PetriAtom
import model.PetriAtomId
import ru.misterpotz.model.atoms.Arc
import ru.misterpotz.model.atoms.Place
import ru.misterpotz.model.atoms.Transition

interface PetriAtomRegistry {
    operator fun get(petriAtomId: PetriAtomId): PetriAtom
    fun getPlace(petriAtomId: PetriAtomId) : Place
    fun getTransition(petriAtomId: PetriAtomId) : Transition
    fun getArc(petriAtomId: PetriAtomId) : Arc
    fun getPlaces() : Iterable<PetriAtomId>
    fun getTransitions() : Iterable<PetriAtomId>
    fun getArcs() : Iterable<PetriAtomId>
    fun PetriAtomId.asNode() : PetriAtom
    var PetriAtomId.subgraphIndex : Int?

    fun PetriAtomId.arcTo(petriAtom: PetriAtomId) : Arc
    operator fun set(petriAtomId: PetriAtomId, petriAtom: PetriAtom)

    fun getSubgraphIndex(petriAtomId: PetriAtomId) : Int?
    fun setSubgraphIndex(petriAtomId: PetriAtomId, subgraphIndex : Int?)
    val iterator : MutableIterator<PetriAtomId>
}

fun createPetriAtomRegistry(elements : Map<PetriAtomId, PetriAtom>) : PetriAtomRegistry {
    return PetriAtomRegistryImpl(elements.toMutableMap())
}
