package ru.misterpotz.ocgena.ocnet.primitives.arcs

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtom
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcType

@Serializable
data class VariableArc(
    override val id: String,
) : Arc() {
    override val arcType: ArcType
        get() = ArcType.VARIABLE

    override fun isSameArcType(other: Arc): Boolean {
        return other is VariableArc
    }

    override fun isSameType(other: PetriAtom): Boolean {
        return other is VariableArc
    }
}
