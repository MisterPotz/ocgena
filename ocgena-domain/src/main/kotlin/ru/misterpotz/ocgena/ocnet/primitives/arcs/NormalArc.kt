package ru.misterpotz.ocgena.ocnet.primitives.arcs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtom
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomType
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcType

@Serializable
@SerialName("arc")
data class NormalArc(
    override val id: String,
    val multiplicity: Int = 1,
) : Arc() {
    override val arcType: ArcType
        get() = ArcType.NORMAL

    override fun isSameArcType(other: Arc): Boolean {
        return other is NormalArc
    }


    override fun isSameType(other: PetriAtom): Boolean {
        return other is NormalArc
    }
}
