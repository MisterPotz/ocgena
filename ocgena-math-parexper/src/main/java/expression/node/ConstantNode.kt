package ru.misterpotz.expression.node

import ru.misterpotz.expression.paramspace.ParameterSpace

class ConstantNode(val value: Double) : MathNode {
    override fun evaluate(parameterSpace: ParameterSpace): Double {
        return value
    }

    override fun toString(): String {
        return printExpr()
    }

    override fun printTree(): String {
        return value.toString()
    }

    override fun printExpr(): String {
        return value.toString()
    }
}