package utils


sealed class Operator() {
    abstract fun expand(operator: Operator) : Operator

    class BinaryPlus(
//    val leftHand: ExpressionNode,
//    val rightHand: ExpressionNode,
    ): Operator() {
        override fun expand(operator: Operator): Operator {
            return when (operator) {
                is BinaryPlus -> BinaryPlus()

            }
        }
    }

    class UnaryPlus() : Operator() {
        override fun expand(operator: Operator): Operator {
            return when (operator) {
                is BinaryPlus -> BinaryPlus()
                is UnaryPlus -> UnaryPlus()
            }
        }
    }

    class BinaryMinus() : Operator() {
        override fun expand(operator: Operator): Operator {
            return when (operator) {
                is BinaryMinus ->
                is BinaryPlus -> TODO()
                is UnaryPlus -> TODO()
            }
        }
    }
}



class Plus(
    val leftHand: ExpressionNode,
    val rightHand: ExpressionNode,
) {
    fun do() : ExpressionNode? {
        if (leftHand.isLeaf() && rightHand.isLeaf()) {
            return leftHand.doBinaryOp("+", rightHand)
        }
        if (leftHand.isLeaf() && rightHand.isLeaf().not()) {

        }
    }
}

class VariableNode(
    var coeff: Int,
    val variable : String
) : ExpressionNode(variable) {
    override fun evaluate(): ExpressionNode {
        return this
    }

    val intOps = setOf("*", "/")
    val varOps = setOf("+", "-")

    override fun doBinaryOp(op: String, rightHand: ExpressionNode): ExpressionNode? {
        if (rightHand is IntNode) {
            if (op !in intOps) return null
            val resultCoeff = when (op) {
                "*" -> coeff * rightHand.number
                "/" -> coeff / rightHand.number
                else -> throw IllegalStateException()
            }
            return VariableNode(resultCoeff, variable)
        }
        if (rightHand is VariableNode) {
            if (op !in varOps) return null
            val resultCoeff = when (op) {
                "+" -> coeff + rightHand.coeff
                "-" -> coeff - rightHand.coeff
                else -> throw IllegalStateException()
            }
            return VariableNode(resultCoeff, variable)
        }

        return null
    }
}

class IntNode(
    val number: Int,
) : ExpressionNode(data = number.toString()) {
    override fun evaluate(): ExpressionNode {
        return this
    }

    val intOps = setOf("+", "-")
    val varOps = setOf("*", "/")

    override fun doBinaryOp(op: String, rightHand: ExpressionNode): ExpressionNode? {
        if (rightHand is IntNode) {
            if (op !in intOps) return null
            val number = when (op) {
                "+" -> number * rightHand.number
                "-" -> number - rightHand.number
                else -> throw IllegalStateException()
            }
            return IntNode(number)
        }
        if (rightHand is VariableNode) {
            if (op !in varOps) return null
            val resultCoeff = when (op) {
                "*" -> rightHand.coeff * number
                "/" -> rightHand.coeff / number
                else -> throw IllegalStateException()
            }
            return VariableNode(resultCoeff, rightHand.variable)
        }
        return null
    }
}

open class ExpressionNode(
    var data: String = "",
    var left: ExpressionNode? = null,
    var right: ExpressionNode? = null,
) {
    fun isLeaf(): Boolean {
        return left == null && right == null
    }

    fun isNumber(): Boolean {
        return data.toIntOrNull() != null
    }

    fun isVariable(): Boolean {
        return data.matches(Regex("[a-zA-Z]+"))
    }

    fun int(): Int {
        return data.toInt()
    }

    fun isBinaryOp() : Boolean {
        return when (data) {
            "+", "-", "*", "/" -> true
            else -> false
        }
    }

    open fun doBinaryOp(op: String, rightHand: ExpressionNode): ExpressionNode? {
        val result = left?.doBinaryOp(op, rightHand)
        if (result != null && result != left) {
            return result
        }
        return this
    }

    open fun evaluate(): ExpressionNode? {
        if (isLeaf() && isNumber()) return IntNode(int())
        if (isLeaf() && isVariable()) return VariableNode(coeff = 1, variable = data)
        if (isLeaf()) return this

        if (isBinaryOp()) {
            val leftEvaluation = left!!.evaluate()
            val rightEvaluation = right!!.evaluate()

            if (leftEvaluation.isLeaf()) {
                leftEvaluation.doBinaryOp(rightEvaluation)
            }
            leftEvaluation.doBinaryOp(op = data, rightEvaluation)
        }

        val newNode = if (leftEvaluation != null && leftEvaluation.isLeaf()
            && rightEvaluation != null && rightEvaluation.isLeaf()
        ) {

        }
    }

    fun print(): String {
        if (left == null && right == null) {
            return data
        }
        return "(" + (left?.print() ?: "") + data + (right?.print() ?: "") + ")"
    }

    override fun toString(): String {
        return """$data
            |${(left?.toString() ?: "").prependIndent()}
            |${(right?.toString() ?: "").prependIndent()}
        """.trimMargin().replace(reg, "")
    }

    companion object {
        val reg = Regex("^\\s*$[\n\r]*", RegexOption.MULTILINE)
    }
};

// Function to create new node
fun newNode(c: String): ExpressionNode {
    return ExpressionNode(c)
}

fun <T> MutableList<T>.getAndRemoveLast(): T {
    return last().also {
        removeLast()
    }
}

class InfixExpressionTreeCreator() {
    fun infixExpression(elements: List<String>): ExpressionNode {
        // Stack to hold nodes
        val nodes: MutableList<ExpressionNode> = mutableListOf()

        // Stack to hold chars
        val ops: MutableList<String> = mutableListOf()

        var lastInsertedWasOp = false

        var newNode: ExpressionNode
        var t1: ExpressionNode
        var t2: ExpressionNode

        val unaryOps = setOf("-", "+")
        val binaryOps = setOf("-", "+", "/", "*")

        // Prioritising the operators
        val binaryOpPriority = mapOf(
            "+" to 1,
            "-" to 1,
            "/" to 2,
            "*" to 2,
        )

        val letterRegexp = Regex("[a-zA-Z\\d]+")
        var treatOpsAsUnary = true

        for (i in elements) {
            if (i == "(") {
                ops.add(i)
                treatOpsAsUnary = true
            } else if (letterRegexp.matches(i)) {
                newNode = newNode(i)
                nodes.add(newNode)

                // consume unary ops
                if (ops.isNotEmpty() && ops.last() in unaryOps && treatOpsAsUnary) {
                    newNode = newNode(ops.getAndRemoveLast())
                    newNode.right = nodes.getAndRemoveLast()
                    nodes.add(newNode)
                }

                treatOpsAsUnary = false
            } else if (i == ")") {
                while (ops.isNotEmpty() && ops.last().let { it in binaryOps } && nodes.size > 1) {
                    newNode = newNode(ops.getAndRemoveLast())
                    newNode.right = nodes.getAndRemoveLast()
                    newNode.left = nodes.getAndRemoveLast()
                    nodes.add(newNode)
                }
                if (ops.isNotEmpty() && ops.last() == "(") {
                    ops.remove("(")
                }
            } else if (i in binaryOps) {
                while (ops.isNotEmpty()
                    && ops.last() != "("
                    && binaryOpPriority[ops.last()]!! >= binaryOpPriority[i]!!
                ) {
                    newNode = newNode(ops.getAndRemoveLast())
                    newNode.right = nodes.getAndRemoveLast()
                    newNode.left = nodes.getAndRemoveLast()
                    nodes.add(newNode)
                }

                // Push s[i] to char stack
                ops.add(i)
            }
        }

        while (ops.isNotEmpty()) {
            newNode = newNode(ops.getAndRemoveLast())
            newNode.right = nodes.getAndRemoveLast()
            newNode.left = nodes.getAndRemoveLast()
            nodes.add(newNode)
        }

        return nodes.last()
    }
}
