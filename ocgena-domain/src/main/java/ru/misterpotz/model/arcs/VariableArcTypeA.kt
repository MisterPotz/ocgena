package ru.misterpotz.model.arcs

import ru.misterpotz.model.atoms.Arc


data class VariableArcTypeA(
    override val id: String,
) : Arc() {
    override fun isSameArcType(other: Arc): Boolean {
        return other is VariableArcTypeA
    }

    override fun toString(): String {
        return "[ $id ]"
    }
}
