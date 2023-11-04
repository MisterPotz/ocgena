package ru.misterpotz.ocgena.ocnet.primitives.arcs

import ru.misterpotz.ocgena.ocnet.primitives.PetriAtom
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc

data class VariableArc(
    override val id: String,
) : Arc() {
    override fun isSameArcType(other: Arc): Boolean {
        return other is VariableArc
    }

    override fun isSameType(other: PetriAtom): Boolean {
        return other is VariableArc
    }

    override fun toString(): String {
        return "[ $id ]"
    }
}
