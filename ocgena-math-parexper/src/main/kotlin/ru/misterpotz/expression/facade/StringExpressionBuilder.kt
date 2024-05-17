package ru.misterpotz.expression.facade

import expression.visitor.EachElementVisitor
import expression.visitor.MathNodeVisitorVariableFinder
import expression.visitor.SimpleVariableConfirmerVisitor
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

val String.fullConvertM : MathNode
    get() = buildExpr(this.trim())

fun MathNode.getVariablesNames() : List<String> {
    val variableNodeVisitorVariableFinder = MathNodeVisitorVariableFinder()
    val eachElementVisitor = EachElementVisitor(listOf(variableNodeVisitorVariableFinder))
    acceptVisitor(eachElementVisitor)
    return variableNodeVisitorVariableFinder.variables.toList()
}

fun MathNode.isSimpleVariable() : Boolean {
    val confirmer = SimpleVariableConfirmerVisitor()
    val eachElementVisitor = EachElementVisitor(listOf(confirmer))
    acceptVisitor(eachElementVisitor)
    return confirmer.isSimpleSingleVariable()
}