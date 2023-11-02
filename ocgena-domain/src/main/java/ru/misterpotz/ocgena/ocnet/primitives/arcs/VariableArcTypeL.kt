package ru.misterpotz.ocgena.ocnet.primitives.arcs

import ru.misterpotz.ocgena.ocnet.primitives.PetriAtom
import model.typel.ArcExpression
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc

data class VariableArcTypeL(
    override val id: String,
    val expression: ArcExpression,
) : Arc() {

    override fun isSameArcType(other: Arc): Boolean {
        return other is VariableArcTypeL
    }

    override fun isSameType(other: PetriAtom): Boolean {
        return other is VariableArcTypeL
    }

    override fun toString(): String {
        return "$expression=>"
    }
}
