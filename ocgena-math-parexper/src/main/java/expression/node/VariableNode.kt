package ru.misterpotz.expression.node

import ru.misterpotz.expression.paramspace.ParameterSpace

class VariableNode(val name : String) : MathNode {
    val unpacked = name.removePrefix("var_")
    override fun evaluate(parameterSpace: ParameterSpace): Double {
        return parameterSpace[unpacked]
    }

    override fun toString(): String {
        return printExpr()
    }

    override fun printTree(): String {
        return name
    }

    override fun printExpr(): String {
        return name
    }


}