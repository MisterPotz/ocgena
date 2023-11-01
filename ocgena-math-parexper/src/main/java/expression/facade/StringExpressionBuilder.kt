package ru.misterpotz.expression.facade

import ru.misterpotz.expression.StringTokenizer
import ru.misterpotz.expression.builder.TokenParseBuilder
import ru.misterpotz.expression.node.MathNode

class StringExpressionBuilder(private val expression: String) {
    private val tokenParser = TokenParseBuilder(
        StringTokenizer.tokenize(expression)
    )

    fun buildExpression(): MathNode {
        return tokenParser.buildExpression()
    }
}

fun buildExpr(expression: String) : MathNode {
    return StringExpressionBuilder(expression).buildExpression()
}

val String.m : MathNode
    get() = buildExpr(this)