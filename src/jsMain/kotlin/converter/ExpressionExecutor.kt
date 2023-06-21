package converter

import ast.*
import utils.ExpressionNode

class ExpressionTreeResolver(private val parentExpressionNode: ExpressionNode) {
    val variable by lazy {

    }

//    private fun findVariable(expressionNode: ExpressionNode): String {
//        if (expressionNode.data.toIntOrNull() == nul
//    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
class ExpressionExecutor(private val rootExpression: RootExpression) {
    private var foundVariable: String? = null
    private val opStack: MutableList<Op> = mutableListOf()
    private val valuesStack: MutableList<Int> = mutableListOf()
    private var toSubstitute: Int? = null
    private var isInSubstituteMode: Boolean = false

    val variable: String
        get() {
            if (foundVariable == null) {
                resolveVariable()
            }
            return foundVariable!!
        }

    enum class Op(val stringOp: String) {
        PLUS("+"), DIV("/"), MULT("*"), MINUS("-");

        companion object {
            private val ops =
                Op.values().fold(mutableMapOf<String, Op>()) { accum, op -> accum[op.stringOp] = op; accum }

            fun convert(op: String): Op {
                return ops[op]!!
            }
        }
    }

    private fun consumeOperations(amount: Int) {
        for (i in (opStack.size - 1) downTo (opStack.size - amount)) {
            val op = opStack.removeLast()
            val rightValue = valuesStack.removeLast()
            val leftValue = valuesStack.removeLast()
            val result = when (op) {
                Op.PLUS -> leftValue + rightValue
                Op.DIV -> leftValue / rightValue
                Op.MULT -> leftValue * rightValue
                Op.MINUS -> leftValue - rightValue
            }
            valuesStack.add(result)
        }
    }

    private fun resolveVariable() {
        isInSubstituteMode = false
        opStack.clear()
        valuesStack.clear()
        parseExpression(rootExpression)
        kotlinx.js.console.log("left ops: $opStack")
        kotlinx.js.console.log("left values $valuesStack")
        opStack.clear()
        valuesStack.clear()
    }

    private fun parseExpression(expression: Expression) {
        if (isFinishedValue(expression)) {
            substituteValue(expression)
        } else {
            parseExpression(expression.head)

            val tails = expression.tail

            for (tail in tails) {
                parseExpressionOp(tail)
            }
            val parsedTails = tails.size
            if (parsedTails > 0) {
                if (isInSubstituteMode) {
                    consumeOperations(parsedTails)
                }
            }
        }
    }

    private fun parseExpressionOp(expressionOp: ExpressionOp) {
        val op = Op.convert(expressionOp.op)
        opStack.add(op)

        val expression = expressionOp.target
        parseExpression(expression)
    }

    private fun substituteValue(head: dynamic) {
        if (isVariable(head)) {
            foundVariable = head.variable as String
            if (isInSubstituteMode) {
                valuesStack.add(toSubstitute!!)
            }
        } else {
            if (isInSubstituteMode) {
                valuesStack.add(head.toString().toInt())
            }
        }
    }

    fun isFinishedValue(expressionElement: Expression): Boolean {
        return !isExpression(expressionElement)
    }

    fun substitute(variable: Int): Int {
        isInSubstituteMode = true
        toSubstitute = variable
        valuesStack.clear()
        opStack.clear()
        parseExpression(rootExpression)
        kotlinx.js.console.log("left ops: $opStack")
        kotlinx.js.console.log("left values $valuesStack")
        return valuesStack.last().also {
            valuesStack.clear()
            opStack.clear()
        }
    }
}
