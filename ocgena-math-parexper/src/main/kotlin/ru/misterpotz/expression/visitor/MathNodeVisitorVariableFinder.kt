package expression.visitor

import ru.misterpotz.expression.node.*

class EachElementVisitor(
    val delegates: List<MathNodeVisitor>
) : MathNodeVisitor {

    private fun doForAll(action: MathNodeVisitor.() -> Unit) {
        for (i in delegates) {
            i.action()
        }
    }

    override fun visitTwoSideOperatorNode(twoSideOperatorNode: TwoSideOperatorNode) {
        doForAll {
            visitTwoSideOperatorNode(twoSideOperatorNode)
        }
        for (i in twoSideOperatorNode.children) {
            i.acceptVisitor(this)
        }
    }

    override fun visitUniOperatorNode(uniOperatorNode: UniOperatorNode) {
        doForAll {
            visitUniOperatorNode(uniOperatorNode)
        }
        for (i in uniOperatorNode.children) {
            i.acceptVisitor(this)
        }
    }

    override fun visitVariableNode(variableNode: VariableNode) {
        doForAll {
            visitVariableNode(variableNode)
        }
    }

    override fun visitConstantNode(constantNode: ConstantNode) {
        doForAll {
            visitConstantNode(constantNode)
        }
    }
}

abstract class NoOpVisitor : MathNodeVisitor {
    override fun visitTwoSideOperatorNode(twoSideOperatorNode: TwoSideOperatorNode) {
    }

    override fun visitUniOperatorNode(uniOperatorNode: UniOperatorNode) {
    }

    override fun visitVariableNode(variableNode: VariableNode) {
    }

    override fun visitConstantNode(constantNode: ConstantNode) {
    }
}

class MathNodeVisitorVariableFinder : NoOpVisitor() {
    val variables = mutableSetOf<String>()

    override fun visitVariableNode(variableNode: VariableNode) {
        val variable = variableNode.unpacked

        variables.add(variable)
    }
}

class SimpleVariableConfirmerVisitor : MathNodeVisitor {
    var singleVariable: Boolean = true
    var variable : String? = null
    var foundInvalidOperation: Boolean = false
    var foundInvalidNode: Boolean = false

    fun isSimpleSingleVariable() : Boolean {
        return singleVariable && variable != null && !foundInvalidNode && !foundInvalidOperation
    }

    override fun visitUniOperatorNode(uniOperatorNode: UniOperatorNode) {
        if (uniOperatorNode.opKey != "+") {
            foundInvalidOperation = true
        }
    }

    override fun visitTwoSideOperatorNode(twoSideOperatorNode: TwoSideOperatorNode) {
        if (twoSideOperatorNode.opKey != "*") {
            foundInvalidOperation = true
        }
    }

    override fun visitVariableNode(variableNode: VariableNode) {
        if (variable == null) {
            variable = variableNode.unpacked
            singleVariable = true
        } else {
            singleVariable = false
        }
    }

    override fun visitConstantNode(constantNode: ConstantNode) {
        if (constantNode.value != 1.0) {
            foundInvalidNode = true
        }
    }
}