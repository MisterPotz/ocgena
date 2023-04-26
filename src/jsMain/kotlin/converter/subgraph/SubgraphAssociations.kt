package converter.subgraph

import ast.Subgraph

class SubgraphAssociations() {
    val namedSubgraphs: MutableMap<String, Subgraph> = mutableMapOf()

    fun rememberSubgraph(subgraph: Subgraph) {
        subgraph.id?.value?.let {
            namedSubgraphs[it] = subgraph
        }
    }

    fun containsSubgraph(subgraph: Subgraph): Boolean {
        return subgraph.id?.value in namedSubgraphs
    }
}
