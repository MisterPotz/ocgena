package ru.misterpotz.ocgena.ocnet.primitives

import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.validation.PetriAtomVisitorDFS

interface SerializableAtom

interface PetriNode : PetriAtom {
    val label: String
    override fun acceptVisitor(visitor: PetriAtomVisitorDFS)

    fun getArcTo(node: PetriAtomId) : PetriAtomId

    fun getArcFrom(node: PetriAtomId) : PetriAtomId

    fun copyWithoutConnections(): PetriNode

    companion object {
        val EMPTY = emptyList<Arc>()
    }
}
