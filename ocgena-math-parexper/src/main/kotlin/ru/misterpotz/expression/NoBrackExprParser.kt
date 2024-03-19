package ru.misterpotz.expression

import ru.misterpotz.expression.node.ConstantNode
import ru.misterpotz.expression.node.MathNode
import ru.misterpotz.expression.node.TwoSideOperatorNode
import ru.misterpotz.expression.node.UniOperatorNode
import ru.misterpotz.expression.node.VariableNode
import ru.misterpotz.expression.builder.TokenParseBuilder

class NoBrackExprParser(
    opsNodes: TokenParseBuilder.OpsNodes
) {
    internal val opStack = opsNodes.ops.toMutableList()
    internal val valueAndNodes = opsNodes.valueAndNodes.toMutableList()
    private val nodeSpace = opsNodes.nodeSpace

    fun fullTransform(): MathNode {
        val lastNodeId = fullTransformAndGetNodeId()
        return nodeSpace[lastNodeId]
    }

    fun fullTransformAndGetNodeId() : String {
        replaceStringValuesWithNodes()
        for (priority in opPriority.keys.sortedDescending()) {
            transformOpsWithPriority(priority)
        }
        return valueAndNodes.first()
    }

    private fun replaceStringValuesWithNodes() {
        for (i in valueAndNodes.indices) {
            valueAndNodes[i] = getOrCreateNodeAtValueIndex(i)
        }
    }

    fun getValuesPointersForOp(opIndex: Int): Values? {
        var valueIndex = 0
        var countedOpIndex = 0

        while (valueIndex < valueAndNodes.size) {
            val op = opStack[countedOpIndex]
            val targetOp = countedOpIndex == opIndex

            if (targetOp) {
                return if (op in unaryOp) {
                    Values.single(valueIndex)
                } else {
                    Values.values(valueIndex, valueIndex + 1)
                }
            }

            if (op in twoSideOp) {
                valueIndex++
                countedOpIndex++
            }
            if (op in unaryOp) {
                countedOpIndex++
            }
        }

        return null
    }

    private fun getOrCreateNodeAtValueIndex(valueIndex: Int): String {
        val lastNode = valueAndNodes[valueIndex]

        if (lastNode in nodeSpace) {
            return lastNode
        }

        return if (processedVariableMatcher.matches(lastNode)) {
            val newNode = VariableNode(name = lastNode)
            nodeSpace.addNodeAndGetId(newNode)
        } else {
            val newNode = ConstantNode(value = lastNode.toDouble())
            nodeSpace.addNodeAndGetId(newNode)
        }
    }

    private fun consumeOpAtIndex(opIndex: Int) {
        opStack.removeAt(opIndex)
    }

    private fun createNodesBasedOnValues(values: Values): List<MathNode> {
        return buildList {
            if (values.singleValue != null) {
                add(nodeSpace[getOrCreateNodeAtValueIndex(values.singleValue)])
            } else {
                add(nodeSpace[getOrCreateNodeAtValueIndex(values.left!!)])
                add(nodeSpace[getOrCreateNodeAtValueIndex(values.right!!)])
            }
        }
    }

    private fun transformOpsWithPriority(priority: Int) {
        val nodeBuilder = NodeBuilder()
        val allowedOps = opPriority[priority]!!
        var opIndex = 0
        while (opIndex < opStack.size) {
            val op = opStack[opIndex]

            if (op !in allowedOps) {
                opIndex++
                continue
            }

            val valueIndex = getValuesPointersForOp(opIndex)!!
            val values = createNodesBasedOnValues(valueIndex)

            val newNode = nodeBuilder
                .setOp(op)
                .setNodes(values)
                .build()

            val newNodeId = nodeSpace.addNodeAndGetId(newNode)

            consumeOpAtIndex(opIndex)
            valueAndNodes[valueIndex.existingIndex] = newNodeId

            if (values.size > 1) {
                valueAndNodes.removeAt(valueIndex.right!!)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class NodeBuilder {
        private var op: String? = null
        private var nodes: List<MathNode>? = null

        fun setOp(op: String): NodeBuilder {
            this.op = op
            return this
        }

        fun setNodes(nodes: List<MathNode>): NodeBuilder {
            this.nodes = nodes
            return this
        }

        fun build(): MathNode {
            val op = op!!
            val nodes = nodes!!

            return if (op in unaryOp) {
                UniOperatorNode(
                    childNode = nodes.first(),
                    opKey = op,
                    operation = opToFun[op]!! as (Double) -> Double
                )
            } else {
                TwoSideOperatorNode(
                    children = nodes.toList(),
                    opKey = op,
                    foldingFunction = opToFun[op]!! as TwoSideFun
                )
            }
        }
    }

    data class Values(
        val left: Int? = null,
        val right: Int? = null,
        val singleValue: Int? = null,
    ) {
        val existingIndex: Int
            get() = left ?: singleValue!!
        val requireRange: IntRange
            get() = left!!..right!!
        val requireSingle: Int
            get() = singleValue!!

        override fun toString(): String {
            return if (left == null) {
                singleValue.toString()
            } else {
                "$left..$right"
            }
        }

        companion object {
            fun single(single: Int): Values {
                return Values(singleValue = single)
            }

            fun values(left: Int, right: Int): Values {
                return Values(left = left, right = right)
            }
        }
    }
}
