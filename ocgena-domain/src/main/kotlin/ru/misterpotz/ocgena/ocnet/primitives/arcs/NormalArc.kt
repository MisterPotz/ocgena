package ru.misterpotz.ocgena.ocnet.primitives.arcs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtom
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcType

@Serializable
@SerialName("arc")
data class NormalArc(
    override val id: String,
    override val arcMeta: NormalArcMeta = NormalArcMeta(1)
) : Arc() {
    val multiplicity: Int
        get() = arcMeta.multiplicity

    override val arcType: ArcType
        get() = ArcType.NORMAL

    override fun isSameArcType(other: Arc): Boolean {
        return other is NormalArc
    }


    override fun isSameType(other: PetriAtom): Boolean {
        return other is NormalArc
    }
}
