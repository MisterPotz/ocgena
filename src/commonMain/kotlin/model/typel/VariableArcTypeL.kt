package model.typel

import model.Arc
import model.PetriNode

data class VariableArcTypeL(
    override val id: String,
    override var arrowNode: PetriNode?,
    override var tailNode: PetriNode?,
    val expression: ArcExpression,
) : Arc() {
    val variable
        get() = expression.variable

    override fun copyWithTailAndArrow(newTail: PetriNode, newArrow: PetriNode): Arc {
        return copy(
            tailNode = newTail,
            arrowNode = newArrow
        )
    }

    override fun isSameArcType(other: Arc): Boolean {
        return other is VariableArcTypeL
    }

    override fun toString(): String {
        return "$expression=>"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as VariableArcTypeL

        if (id != other.id) return false
        if (arrowNode?.id != other.arrowNode?.id) return false
        if (tailNode?.id != other.tailNode?.id) return false
        return expression == other.expression
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (arrowNode?.id?.hashCode() ?: 0)
        result = 31 * result + (tailNode?.id?.hashCode() ?: 0)
        result = 31 * result + expression.hashCode()
        return result
    }
}