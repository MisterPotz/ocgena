package converter

import ast.ASTBaseNode
import ast.Edge
import ast.EdgeRHSElement
import ast.EdgeSubgraph
import ast.Node
import ast.NodeRef
import ast.OpTypes
import ast.Types
import dsl.OCNetFacadeBuilder
import dsl.OCScope

class OCDotToDomainConverter() {
    fun convert(dslElementsContainer: DSLElementsContainer): OCNetFacadeBuilder.BuiltOCNet {
        val ocNetFacadeBuilder = OCNetFacadeBuilder()
        val resultOfBuildAttempt = ocNetFacadeBuilder.tryBuildModel {
            for (astObjectType in dslElementsContainer.savedObjectTypes) {
                objectType(astObjectType.key)
            }
            for (astPlace in dslElementsContainer.savedPlaces) {
                place(astPlace.key) {
                    // initialize place data
                }
            }

            for (astTransition in dslElementsContainer.savedTransitions) {
                transition(astTransition.key) {
                    // initialize transition data
                }
            }

            for (edge in dslElementsContainer.savedEdgeBlocks) {
                processEdge(edge)
            }
        }
        return resultOfBuildAttempt
    }

    fun OCScope.processEdge(edge: Edge) {
        val connector = Connector(edge)
        with(connector) {
            tryConnectAll()
        }
    }

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
            if (isEdgeRhs(fromOrTo)) {
                return getAllNodeRefsFromEdgeRhs(fromOrTo)
            } else if (isNodeRef(fromOrTo)) {
                return listOf((fromOrTo as NodeRef).id.value)
            }
            return listOf()
        }

        fun OCScope.tryConnectAll() {
            val iterable = createToFromIterator()

            for (toFrom in iterable) {
                val allNodesOfFrom = getAllNodeRefsFromEdgeFromOrTo(toFrom.from)
                val allNodesOfTo = getAllNodeRefsFromEdgeFromOrTo(toFrom.to)

                for (fromNode in allNodesOfFrom) {
                    val fromNode = elementByLabel(fromNode) ?: continue
                    for (toNode in allNodesOfTo) {
                        val toNode = elementByLabel(toNode) ?: continue
                        when (toFrom.to.edgeop.type) {
                            OpTypes.Normal -> fromNode.arcTo(toNode)
                            OpTypes.Variable -> fromNode.variableArcTo(toNode)
                        }
                    }
                }
            }

        }

        class ToFrom(val from: dynamic, val to: EdgeRHSElement) {
            fun isCorrectEdgeFrom(): Boolean {
                return isNodeRef(from) || isEdgeRhs(from)
            }
        }
    }

    class EdgeToFromIterator(val edge: Edge) : Iterator<Connector.ToFrom> {
        var currentToIndex = 0
        override fun hasNext(): Boolean {
            return currentToIndex < edge.targets.size
        }

        override fun next(): Connector.ToFrom {
            val from = if (currentToIndex == 0) {
                edge.from
            } else {
                edge.targets[currentToIndex - 1]
            }

            val to = edge.targets[currentToIndex]
            currentToIndex++

            return Connector.ToFrom(from, to)
        }
    }

    companion object {
        fun isEdgeRhs(from: dynamic): Boolean {
            return when ((from as? EdgeRHSElement)?.edgeop?.type) {
                OpTypes.Normal, OpTypes.Variable -> true
                else -> false
            }
        }

        fun isNodeRef(from: dynamic): Boolean {
            return from.type == Types.NodeRef
        }
    }
}
