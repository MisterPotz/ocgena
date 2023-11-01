package ru.misterpotz.model.arcs

import model.typel.ArcExpression
import ru.misterpotz.model.atoms.Arc

data class VariableArcTypeL(
    override val id: String,
    val expression: ArcExpression,
) : Arc() {
    val variable
        get() = expression.variable

    override fun isSameArcType(other: Arc): Boolean {
        return other is VariableArcTypeL
    }

    override fun toString(): String {
        return "$expression=>"
    }
}
