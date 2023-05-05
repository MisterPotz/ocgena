package model.typel

abstract class Expression(
    override val variable: String?,
    val stringExpr: String?
) : ArcExpression {
    abstract override fun substitute(variableValue: Int): Int
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Expression

        if (variable != other.variable) return false
        return stringExpr == other.stringExpr
    }

    override fun hashCode(): Int {
        var result = variable.hashCode()
        result = 31 * result + stringExpr.hashCode()
        return result
    }

    override fun toString(): String {
        return stringExpr ?: "no expr"
    }
}
