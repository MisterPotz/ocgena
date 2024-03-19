package ru.misterpotz.ocgena.ocnet.primitives.arcs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.misterpotz.expression.facade.fullConvertM
import ru.misterpotz.expression.facade.getVariablesNames
import ru.misterpotz.expression.node.MathNode
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtom
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcType

data class VariableArcMeta(
    val variableName: String?
) : ArcMeta {
    override fun toString(): String {
        return "var. l. $variableName"
    }

    override fun shortString(): String {
        return "vl${variableName ?: " "}"
    }
}

object VariableArcMetaTypeA : ArcMeta {
    override fun toString(): String {
        return "var. aalst"
    }

    override fun shortString(): String {
        return "va "
    }
}

@Serializable
@SerialName("vararc")
data class VariableArc(
    override val id: String,
    @SerialName("math_exp")
    val mathExpression: String? = null,
) : Arc() {
    override val arcType: ArcType
        get() = ArcType.VARIABLE

    val mathNode: MathNode? by lazy(LazyThreadSafetyMode.NONE) {
        mathExpression?.fullConvertM
    }

    val variableName: String? by lazy(LazyThreadSafetyMode.NONE) {
        mathNode?.getVariablesNames()?.also {
            require(it.size == 1) {
                "variable arcs must have no mroe than one variable"
            }
        }?.first()
    }
    override val arcMeta: ArcMeta by lazy(LazyThreadSafetyMode.NONE) {
        VariableArcMeta(variableName)
    }

    override fun isSameArcType(other: Arc): Boolean {
        return other is VariableArc
    }

    override fun isSameType(other: PetriAtom): Boolean {
        return other is VariableArc
    }
}
