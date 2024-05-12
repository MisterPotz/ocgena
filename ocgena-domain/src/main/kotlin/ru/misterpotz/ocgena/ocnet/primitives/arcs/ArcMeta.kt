package ru.misterpotz.ocgena.ocnet.primitives.arcs

import kotlinx.serialization.Serializable

@Serializable
sealed interface ArcMeta {
    fun shortString(): String

    fun isVar(): Boolean {
        return when (this) {
            AalstVariableArcMeta, is LomazovaVariableArcMeta -> true
            is NormalArcMeta -> false
        }
    }
}
