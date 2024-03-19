package ru.misterpotz.expression

import ru.misterpotz.expression.node.ConstantNode
import ru.misterpotz.expression.node.MathNode
import ru.misterpotz.expression.node.TwoSideOperatorNode
import ru.misterpotz.expression.node.UniOperatorNode
import ru.misterpotz.expression.node.VariableNode

operator fun MathNode.unaryPlus() : MathNode {
    return UniOperatorNode(
        childNode = this,
        opKey = "+",
        operation = MathsFuns.uniPlusFun
    )
}

val Number.m : MathNode
    get() = ConstantNode(this.toDouble())
val String.m : MathNode
    get() = VariableNode(this)

operator fun MathNode.plus(mathNode: MathNode) : MathNode {
    val newOpKey = "+"
    val newOpFun = MathsFuns.plusFun

    return addToTwoSideMathNode(newOpKey, newOpFun, mathNode)
}

operator fun MathNode.minus(mathNode: MathNode) : MathNode {
    val newOpKey = "-"
    val newOpFun = MathsFuns.minusFun

    return addToTwoSideMathNode(newOpKey, newOpFun, mathNode)
}

operator fun MathNode.times(mathNode: MathNode) : MathNode {
    val newOpKey = "*"
    val newOpFun = MathsFuns.productFun

    return addToTwoSideMathNode(newOpKey, newOpFun, mathNode)
}

operator fun MathNode.unaryMinus() : MathNode {
    return UniOperatorNode(
        childNode = this,
        opKey = "-",
        operation = MathsFuns.uniMinusFun
    )
}

fun MathNode.addToTwoSideMathNode(newOpKey: String, opFun: TwoSideFun, mathNode: MathNode): MathNode {
    val children = if (this is TwoSideOperatorNode && opKey == newOpKey) {
        children.toMutableList().apply {
            add(mathNode)
        }
    } else {
        mutableListOf(this, mathNode)
    }

    return TwoSideOperatorNode(
        children = children,
        opKey = newOpKey,
        foldingFunction = opFun,
    )
}
