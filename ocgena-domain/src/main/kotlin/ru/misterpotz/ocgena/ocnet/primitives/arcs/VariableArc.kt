package ru.misterpotz.ocgena.ocnet.primitives.arcs

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.misterpotz.expression.facade.fullConvertM
import ru.misterpotz.expression.facade.getVariablesNames
import ru.misterpotz.expression.node.MathNode
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtom
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcType

@Serializable
@SerialName("lomazova")
data class LomazovaVariableArcMeta(
    @SerialName("math_exp")
    val mathExpression: String,
) : ArcMeta {
    val mathNode: MathNode by lazy(LazyThreadSafetyMode.NONE) {
        mathExpression.fullConvertM
    }

    val variableName: String by lazy(LazyThreadSafetyMode.NONE) {
        mathNode.getVariablesNames().also {
            require(it.size == 1) {
                "variable arcs must have no mroe than one variable"
            }
        }.first()
    }

    override fun toString(): String {
        return "v/l [${mathExpression ?: ""}]"
    }

    override fun shortString(): String {
        return "vl${variableName ?: " "}"
    }
}

@Serializable
@SerialName("aalst")
data object AalstVariableArcMeta : ArcMeta {
    override fun toString(): String {
        return "v/a"
    }

    override fun shortString(): String {
        return "va "
    }
}

@Serializable
@SerialName("vararc")
data class VariableArc(
    override val id: String,
    @Contextual
    override val arcMeta: ArcMeta,
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
