package ru.misterpotz.ocgena.ocnet.primitives.arcs
data class NormalArcMeta(val multiplicity: Int) : ArcMeta {
    override fun toString(): String {
        return "norm.$multiplicity"
    }

    override fun shortString() : String {
        return "n  "
    }
}
