package model

import ru.misterpotz.model.atoms.Arc
import ru.misterpotz.model.validation.PetriAtomVisitorDFS

interface SerializableAtom

interface PetriNode : ConsistencyCheckable, PetriAtom {
    val label: String
    override fun acceptVisitor(visitor: PetriAtomVisitorDFS)

    fun getArcTo(node: PetriAtomId) : PetriAtomId

    fun getArcFrom(node: PetriAtomId) : PetriAtomId

    fun copyWithoutConnections(): PetriNode

    companion object {
        val EMPTY = emptyList<Arc>()
    }
}
