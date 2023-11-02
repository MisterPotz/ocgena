package model

import java.io.Serializable

typealias PetriAtomId = String

interface PetriAtom : Serializable {
    val id : PetriAtomId
    fun acceptVisitor(visitor: PetriAtomVisitorDFS)
    fun isSameType(other: PetriAtom): Boolean

    companion object {
        const val UNASSIGNED_SUBGRAPH_INDEX = -1
    }
}
