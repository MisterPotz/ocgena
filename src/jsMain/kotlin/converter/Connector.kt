package converter

import ast.*
import dsl.OCScope

class Connector(private val edge: Edge) {

    private fun createToFromIterator(): Iterator<ToFrom> {
        return EdgeToFromIterator(edge)
    }

    private fun isEdgeRhsTargetNodeRef(edgeRHSElement: EdgeRHSElement): Boolean {
        return (edgeRHSElement.id as ASTBaseNode).type == Types.NodeRef
    }

    private fun isEdgeRhsTargetEdgeSubgraph(edgeRHSElement: EdgeRHSElement): Boolean {
        return (edgeRHSElement.id as ASTBaseNode).type == Types.EdgeSubgraph
    }

    private fun getAllNodeRefsFromEdgeRhs(edgeRHSElement: EdgeRHSElement): List<String> {
        if (isEdgeRhsTargetNodeRef(edgeRHSElement)) {
            return listOf((edgeRHSElement.id as NodeRef).id.value)
        } else if (isEdgeRhsTargetEdgeSubgraph(edgeRHSElement)) {
            val edgeSubgraph = edgeRHSElement.id as EdgeSubgraph
            val allSubgraphNodeLabels = edgeSubgraph.body.filter {
                it.type == Types.Node
            }.map { (it as Node).id.value }

            return allSubgraphNodeLabels
        }
        return listOf()
    }

    private fun getAllNodeRefsFromEdgeFromOrTo(fromOrTo: dynamic): List<String> {
        if (OCDotToDomainConverter.isEdgeRhs(fromOrTo)) {
            return getAllNodeRefsFromEdgeRhs(fromOrTo)
        } else if (OCDotToDomainConverter.isNodeRef(fromOrTo)) {
            return listOf((fromOrTo as NodeRef).id.value)
        }
        return listOf()
    }

    fun OCScope.tryConnectAll() {
        val iterable = createToFromIterator()

        for (edgeFromAndEdgeTarget in iterable) {
            val allNodesOfFrom = getAllNodeRefsFromEdgeFromOrTo(edgeFromAndEdgeTarget.from)
            val allNodesOfTo = getAllNodeRefsFromEdgeFromOrTo(edgeFromAndEdgeTarget.to)

            for (fromNode in allNodesOfFrom) {
                val fromNode = elementByLabel(fromNode) ?: continue
                for (toNode in allNodesOfTo) {
                    val toNode = elementByLabel(toNode) ?: continue
                    val number = edgeFromAndEdgeTarget.to.edgeop.params?.number?.toInt()
                    when (edgeFromAndEdgeTarget.to.edgeop.type) {
                        OpTypes.Normal -> fromNode.arcTo(toNode) {
                            if (number != null) {
                                this.multiplicity = number
                            }
                        }
                        OpTypes.Variable -> fromNode.variableArcTo(toNode)
                    }
                }
            }
        }

    }

    class ToFrom(val from: dynamic, val to: EdgeRHSElement) {
        fun isCorrectEdgeFrom(): Boolean {
            return OCDotToDomainConverter.isNodeRef(from) || OCDotToDomainConverter.isEdgeRhs(from)
        }
    }
}
