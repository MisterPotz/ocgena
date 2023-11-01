package ru.misterpotz.expression.node

import ru.misterpotz.expression.paramspace.EmptyParameterSpace
import ru.misterpotz.expression.paramspace.ParameterSpace

interface MathNode {
    fun evaluate(parameterSpace: ParameterSpace = EmptyParameterSpace) : Double
    fun printTree() : String
    fun printExpr() : String
}

interface OperatorNode : MathNode {
    val children : List<MathNode>
    val opKey : String
}

