package ru.misterpotz.expression.node

import ru.misterpotz.expression.TwoSideFun
import ru.misterpotz.expression.paramspace.ParameterSpace

class TwoSideOperatorNode(
    override val children: List<MathNode>,
    override val opKey: String,
    val foldingFunction : TwoSideFun,
) : OperatorNode {

    override fun toString(): String {
        return printExpr()
    }

    override fun evaluate(parameterSpace: ParameterSpace): Double {
        return children.subList(1, children.size).fold(children.first().evaluate(parameterSpace)) { accum, child ->
            foldingFunction(accum, child.evaluate(parameterSpace))
        }
    }

    override fun printTree(): String {
        return """multi $opKey:
            |${children.joinToString("\n") { it.printTree() }.prependIndent("\t")}
        """.trimMargin()
    }

    override fun printExpr(): String {
        return children.joinToString(separator = " $opKey ") { "(" + it.printExpr() + ")" }
    }
}