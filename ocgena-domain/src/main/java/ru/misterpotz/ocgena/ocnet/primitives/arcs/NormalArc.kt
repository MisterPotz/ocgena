package ru.misterpotz.ocgena.ocnet.primitives.arcs

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtom
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc

@Serializable
data class NormalArc(
    override val id: String,
    val multiplicity: Int = 1,
) : Arc() {
    override fun isSameArcType(other: Arc): Boolean {
        return other is NormalArc
    }

    override fun isSameType(other: PetriAtom): Boolean {
        return other is NormalArc
    }
}
