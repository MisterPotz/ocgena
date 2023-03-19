package dsl

class SubgraphDelegate(
    private val scopeAccessibleEntities: ScopeAccessibleEntities,
    private val placeCreator: PlaceCreator,
    private val transitionCreator: TransitionCreator,
) : SubgraphConnector {
    val subgraphStruct: EntitiesCreatedInSubgraph = EntitiesCreatedInSubgraph()
    private fun createLabelForSubgraph(label: String?): String {
        val subgraphIdIssuer = scopeAccessibleEntities.subgraphIdIssuer
        subgraphIdIssuer.newIntId()
        return label ?: subgraphIdIssuer.lastLabelId
    }

    private fun recordSubgraphToThisScope(newSubgraphDSL: SubgraphDSL) {
        subgraphStruct.subgraphs[newSubgraphDSL.label] = newSubgraphDSL
    }

    private fun createSubgraph(label: String?, block: SubgraphDSL.() -> Unit) : SubgraphDSL {
        val entitiesCreatedInSubgraph = EntitiesCreatedInSubgraph()
        val subgraphConnectionResolver = SubgraphConnectionResolver()

        val newSubgraph = SubgraphImpl(
            label = createLabelForSubgraph(label),
            placeDelegate = SubgraphPlaceDelegate(
                entitiesCreatedInSubgraph = entitiesCreatedInSubgraph,
                placeCreator = placeCreator
            ),
            transitionDelegate = SubgraphTransitionDelegate(
                entitiesCreatedInSubgraph = entitiesCreatedInSubgraph,
                transitionCreator = transitionCreator,
            ),
            arcDelegate = SubgraphArcDelegate(
                arcContainer = scopeAccessibleEntities,
                subgraphConnectionResolver = subgraphConnectionResolver,
            ),
            subgraphDelegate = SubgraphDelegate(
                scopeAccessibleEntities = scopeAccessibleEntities,
                placeCreator = placeCreator,
                transitionCreator = transitionCreator
            ),
            subgraphConnectionResolver = subgraphConnectionResolver
        )

        newSubgraph.block()
        return newSubgraph
    }

    override fun subgraph(label: String?, block: SubgraphDSL.() -> Unit): SubgraphDSL {
        val newSubgraph = createSubgraph(label, block)
        recordSubgraphToThisScope(newSubgraph)
        return newSubgraph
    }

    override fun SubgraphDSL.connectTo(linkChainDSL: LinkChainDSL): HasLast {
        return this.connectToLeftOf(linkChainDSL)
    }

    override fun HasElement.connectTo(subgraphDSL: SubgraphDSL): SubgraphDSL {
        return subgraphDSL.connectOnRightTo(this)
    }
}
