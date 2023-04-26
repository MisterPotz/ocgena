package converter

import ast.Edge
import ast.Node
import ast.Subgraph
import converter.subgraph.SubgraphAssociations

class StructureContainer() {
    private val places: MutableMap<String, Node> = mutableMapOf()
    private val transitions: MutableMap<String, Node> = mutableMapOf()

    private val edgeBlocks: MutableList<Edge> = mutableListOf()

    private val subgraphAssociations: SubgraphAssociations = SubgraphAssociations()

    val savedPlaces: Map<String, Node>
        get() = places
    val savedTransitions: Map<String, Node>
        get() = transitions

    val savedEdgeBlocks: List<Edge>
        get() = edgeBlocks

    fun rememberPlace(place: Node) {
        places[place.id.value] = place
    }
    fun rememberSubgraph(subgraph: Subgraph) {
        subgraphAssociations.rememberSubgraph(subgraph)
    }

    fun rememberTransition(transition: Node) {
        transitions[transition.id.value] = transition
    }

    fun rememberEdgeBlock(edge: Edge) {
        edgeBlocks.add(edge)
    }
}
