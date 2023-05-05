package converter

import ast.*

class Connector(
    private val edge: Edge,
    val conversionEntitiesCreator: ConversionEntitiesCreator,
    val arcConversionCreator: ArcConversionCreator
) {

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

    fun tryConnectAll() {
        val iterable = createToFromIterator()

        for (edgeFromAndEdgeTarget in iterable) {
            val allNodesOfFrom = getAllNodeRefsFromEdgeFromOrTo(edgeFromAndEdgeTarget.from)
            val allNodesOfTo = getAllNodeRefsFromEdgeFromOrTo(edgeFromAndEdgeTarget.to)

            for (fromNode in allNodesOfFrom) {
                for (toNode in allNodesOfTo) {
                    val newArc  = arcConversionCreator.createArc(
                        fromNode,
                        toNode,
                        edgeFromAndEdgeTarget.to
                    )
                    conversionEntitiesCreator.recordArc(newArc)
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

