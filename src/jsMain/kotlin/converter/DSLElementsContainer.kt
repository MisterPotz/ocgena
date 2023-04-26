package converter

import ast.Edge
import ast.Node
import ast.Subgraph
import converter.subgraph.SubgraphAssociations
import dsl.OCScopeImpl

class DSLElementsContainer(val ocScopeImpl: OCScopeImpl) {
    private val places: MutableMap<String, Node> = mutableMapOf()

    private val objectTypes: MutableMap<String, Node> = mutableMapOf()

    private val transitions: MutableMap<String, Node> = mutableMapOf()

    private val edgeBlocks: MutableList<Edge> = mutableListOf()

    val subgraphAssociations: SubgraphAssociations = SubgraphAssociations()

    val placeToInitialMarking: MutableMap<String, Int> = mutableMapOf()
    val placeToObjectType: MutableMap<String, String> = mutableMapOf()
    val inputPlaceLabels: MutableList<String> = mutableListOf()
    val outputPlaceLabels: MutableList<String> = mutableListOf()

    val savedObjectTypes: Map<String, Node>
        get() = objectTypes
    val savedPlaces: Map<String, Node>
        get() = places
    val savedTransitions: Map<String, Node>
        get() = transitions

    val savedEdgeBlocks: List<Edge>
        get() = edgeBlocks

    fun rememberPlace(place: Node) {
        places[place.id.value] = place
    }

    fun rememberInitialMarkingForPlace(placeLabel: String, initialTokens: Int) {
        console.log("saving $placeLabel initial tokens $initialTokens")
        placeToInitialMarking[placeLabel] = initialTokens
    }

    fun rememberPlaceIsInput(placeLabel: String) {
        console.log("remembered input $placeLabel")
        inputPlaceLabels.add(placeLabel)
    }

    fun recallIfPlaceIsInput(placeLabel: String): Boolean {
        val input = inputPlaceLabels.find { it == placeLabel } != null
        console.log("place $placeLabel is input $input")
        return input
    }

    fun recallIfPlaceIsOutput(placeLabel: String): Boolean {
        val output = outputPlaceLabels.find { it == placeLabel } != null
        console.log("place $placeLabel is output $output")
        return output
    }

    fun rememberPlaceIsOutput(placeLabel: String) {
        console.log("remembered output $placeLabel")
        outputPlaceLabels.add(placeLabel)
    }

    fun rememberObjectTypeForPlace(placeLabel: String, objectTypeLabel: String) {
        placeToObjectType[placeLabel] = objectTypeLabel
    }

    fun rememberSubgraph(subgraph: Subgraph) {
        subgraphAssociations.rememberSubgraph(subgraph)
    }

    fun rememberObjectType(objectType: Node) {
        objectTypes[objectType.id.value] = objectType
    }

    fun recallObjectTypeForPlace(placeLabel: String): String? {
        return placeToObjectType[placeLabel]
    }

    fun recallInitialTokensForPlace(placeLabel: String): Int? {
        val value = placeToInitialMarking[placeLabel]
        console.log("for place $placeLabel have initial $value")
        return value
    }

    fun rememberTransition(transition: Node) {
        transitions[transition.id.value] = transition
    }

    fun rememberEdgeBlock(edge: Edge) {
        edgeBlocks.add(edge)
    }
}
