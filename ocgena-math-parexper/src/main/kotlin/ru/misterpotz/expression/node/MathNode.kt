package ru.misterpotz.expression.node

import ru.misterpotz.expression.paramspace.EmptyParameterSpace
import ru.misterpotz.expression.paramspace.ParameterSpace

interface MathNode {
    fun evaluate(parameterSpace: ParameterSpace = EmptyParameterSpace) : Double
    fun acceptVisitor(mathNodeVisitor: MathNodeVisitor)
    fun printTree() : String
    fun printExpr() : String
}

interface MathNodeVisitor {
    fun visitTwoSideOperatorNode(twoSideOperatorNode: TwoSideOperatorNode)
    fun visitUniOperatorNode(uniOperatorNode: UniOperatorNode)
    fun visitVariableNode(variableNode: VariableNode)
    fun visitConstantNode(constantNode: ConstantNode)
}

interface OperatorNode : MathNode {
    val children : List<MathNode>
    val opKey : String
}

