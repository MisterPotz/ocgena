package ru.misterpotz.expression.node

import ru.misterpotz.expression.paramspace.ParameterSpace

class UniOperatorNode(private val childNode : MathNode, override val opKey: String, private val operation : (value : Double) -> Double,

                      ): OperatorNode {
    override val children: List<MathNode> = listOf(childNode)
    override fun evaluate(parameterSpace: ParameterSpace): Double {
        val value = childNode.evaluate(parameterSpace)
        return operation(value)
    }

    override fun toString(): String {
        return printExpr()
    }

    override fun printTree(): String {
        return """uni $opKey: 
            |${childNode.printTree().prependIndent("\t")}
        """.trimMargin()
    }

    override fun printExpr(): String {
        return "$opKey(${childNode.printExpr()})"
    }
}