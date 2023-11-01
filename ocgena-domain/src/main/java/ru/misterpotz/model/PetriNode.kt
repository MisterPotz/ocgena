package model

import ru.misterpotz.model.atoms.Arc

interface SerializableAtom

interface PetriNode : ConsistencyCheckable, PetriAtom {
    val label: String
    fun isSameType(other: PetriNode): Boolean
    override fun acceptVisitor(visitor: PetriAtomVisitorDFS)

    fun getArcTo(node: PetriAtomId) : PetriAtomId

    fun getArcFrom(node: PetriAtomId) : PetriAtomId

    fun copyWithoutConnections(): PetriNode

    companion object {
        val EMPTY = emptyList<Arc>()
    }
}
