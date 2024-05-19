package ru.misterpotz.ocgena.ocnet.primitives

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import ru.misterpotz.ocgena.validation.PetriAtomVisitorDFS
import java.io.Serializable

typealias PetriAtomId = String

@kotlinx.serialization.Serializable
enum class PetriAtomType {
    @SerialName("place")
    PLACE,
    @SerialName("arc")
    ARC,
    @SerialName("vararc")
    VAR_ARC,
    @SerialName("transition")
    TRANSITION
}
interface PetriAtom : Serializable {
    val id : PetriAtomId
    fun acceptVisitor(visitor: PetriAtomVisitorDFS)
    fun isSameType(other: PetriAtom): Boolean

    companion object {
        const val UNASSIGNED_SUBGRAPH_INDEX = -1
    }
}
