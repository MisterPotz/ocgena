package ru.misterpotz.ocgena.ocnet.primitives.arcs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("normal")
data class NormalArcMeta(val multiplicity: Int) : ArcMeta {
    override fun toString(): String {
        return "$multiplicity"
    }

    override fun shortString() : String {
        return "n  "
    }
}
