package ru.misterpotz.expression

import ru.misterpotz.expression.node.MathNode
import ru.misterpotz.expression.node.TwoSideOperatorNode

object MathsFuns {
    fun minus(nodes : List<MathNode>): TwoSideOperatorNode {
        return TwoSideOperatorNode(nodes, "-", minusFun)
    }

    fun plus(nodes : List<MathNode>) : TwoSideOperatorNode {
        return TwoSideOperatorNode(nodes, "+", plusFun)
    }

    fun product(nodes : List<MathNode>) : TwoSideOperatorNode {
        return TwoSideOperatorNode(nodes, "*", productFun)
    }

    val uniMinusFun : UniSideFun = { value : Double ->
        -value
    }
    val uniPlusFun : UniSideFun = { value : Double ->
        +value
    }

    val minusFun: TwoSideFun = { left : Double, right : Double ->
        left - right
    }

    val plusFun : TwoSideFun = { left : Double, right : Double ->
        left + right
    }

    val productFun : TwoSideFun = { left : Double, right : Double ->
        left * right
    }
}