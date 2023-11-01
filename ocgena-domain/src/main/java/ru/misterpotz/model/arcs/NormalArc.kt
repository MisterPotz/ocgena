package ru.misterpotz.model.arcs

import kotlinx.serialization.Serializable
import ru.misterpotz.model.atoms.Arc

@Serializable
data class NormalArc(
    override val id: String,
    val multiplicity: Int = 1,
) : Arc() {
    override fun isSameArcType(other: Arc): Boolean {
        return other is NormalArc
    }
}
