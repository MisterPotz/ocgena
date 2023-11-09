package ru.misterpotz.ocgena.ocnet.primitives.arcs

import kotlinx.serialization.Serializable

@Serializable
data class NormalArcMeta(val multiplicity: Int) : ArcMeta {
    override fun toString(): String {
        return "$multiplicity"
    }

    override fun shortString() : String {
        return "n  "
    }
}
