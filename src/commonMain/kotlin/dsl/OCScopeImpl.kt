package dsl


private class GroupIdCreatorSetupper() {
    fun setupGroupIdCreator(groupsIdCreator: GroupsIdCreator) {
        groupsIdCreator.addPatternIdCreatorFor("t", startIndex = 1) {
            "t$it"
        }
        groupsIdCreator.addPatternIdCreatorFor("subgraphs", startIndex = 1) {
            "subgr$it"
        }
    }
}

class OCScopeImplCreator() {
    private val groupIdCreator: GroupsIdCreator = GroupsIdCreator().also {
        GroupIdCreatorSetupper().setupGroupIdCreator(it)
    }
    val scopeEntities = ScopeAccessibleEntities(groupsIdCreator = groupIdCreator, parentScopeEntities = null)
    val objectIdCreator: PatternIdCreator
        get() = groupIdCreator.patternIdCreatorFor("objects")

    val objectTypeCreator = ObjectTypeCreator(
        objectTypesContainer = scopeEntities,
        objectTypeIdCreator = objectIdCreator,
        groupsIdCreator = groupIdCreator
    )
    val defaultObjectType: ObjectTypeDSL =
        objectTypeCreator.createObjectType(label = "ot", placeNameCreator = { "p$it" })
    val subgraphIdCreator : PatternIdCreator
        get() = groupIdCreator.patternIdCreatorFor("subgraphs")
    val arcDelegate = ArcDelegate(arcContainer = scopeEntities)
    val placeDelegate = PlaceDelegate(
        placeCreator = PlaceCreator(
            scopeType = defaultObjectType,
            placesContainer = scopeEntities,
            groupIdCreator = groupIdCreator
        )
    )
    val transitionDelegate = TransitionDelegate(
        transitionCreator = TransitionCreator(
            transitionContainer = scopeEntities
        )
    )
}

class SubgraphDelegate(
    scopeAccessibleEntities: ScopeAccessibleEntities,

    ) : SubgraphConnector {
    val subgraphStruct: EntitiesCreatedInSubgraph = EntitiesCreatedInSubgraph()

    override fun SubgraphDSL.connectTo(linkChainDSL: LinkChainDSL): HasLast {
        TODO("Not yet implemented")
    }

    override fun LinkChainDSL.connectTo(subgraphDSL: SubgraphDSL): SubgraphDSL {
        TODO("Not yet implemented")
    }

    override fun subgraph(label: String?, block: SubgraphDSL.() -> Unit): SubgraphDSL {

    }

}

class OCScopeImpl(
    private val rootScope: OCScopeImpl? = null,
    override val scopeType: ObjectTypeDSL,
    private val scopeEntities: ScopeAccessibleEntities,
    private val groupsIdCreator: GroupsIdCreator,
    private val arcDelegate: ArcDelegate,
    private val placeDelegate: PlaceDelegate,
    private val transitionDelegate: TransitionDelegate,
    private val objectTypeDelegate: ObjectTypeDelegate,
) : TypeScope,
    ArcsAcceptor by arcDelegate,
    PlaceAcceptor by placeDelegate,
    TransitionAcceptor by transitionDelegate,
    ObjectTypeAcceptor by objectTypeDelegate,
    SubgraphConnector {


    private val subgraphStruct = EntitiesCreatedInSubgraph(
        places = mutableMapOf(),
        transitions = mutableMapOf(),
        subgraphs = mutableMapOf()
    )

    override fun subgraph(label: String?, block: SubgraphDSL.() -> Unit): SubgraphDSL {
        val newSubgraph = internalCreateSubgraph(label, block)
        recordSubgraphToThisScope(newSubgraph)
        return newSubgraph
    }


    private fun recordSubgraphToThisScope(newSubgraphDSL: SubgraphDSL) {
        subgraphStruct.subgraphs[newSubgraphDSL.label] = newSubgraphDSL
    }

    internal fun internalCreateSubgraph(label: String?, block: SubgraphDSL.() -> Unit): SubgraphDSL {
        val subgraphIdIssuer = scopeEntities.subgraphIdIssuer
        subgraphIdIssuer.newIntId()

        val inNode = UnresolvedHasLast()
        val outNode = UnresolvedHasFirst()
        val inputNodeDependentCommands = mutableListOf<() -> Unit>()
        val outputNodeDependentCommands = mutableListOf<() -> Unit>()
        val newSubgraph = SubgraphImpl(
            label = label ?: subgraphIdIssuer.lastLabelId,
            rootScope = this,
            inNodeSpec = inNode,
            outNodeSpec = outNode,
            inputNodeDependentCommands = inputNodeDependentCommands,
            outputNodeDependentCommands = outputNodeDependentCommands,
            placeDelegate = placeDelegate,
            transitionDelegate = transitionDelegate,
            arcDelegate = SubgraphArcDelegate(
                arcContainer = scopeEntities,
                inNode = inNode,
                outNode = outNode,
                inputNodeDependentCommands = inputNodeDependentCommands,
                outputNodeDependentCommands = outputNodeDependentCommands,
            )
        )

        newSubgraph.block()
        // don't add this subgraph to local subgraphs
        return newSubgraph
    }

    override fun LinkChainDSL.connectTo(subgraphDSL: SubgraphDSL): SubgraphDSL {

    }

    override fun SubgraphDSL.connectTo(linkChainDSL: LinkChainDSL): HasLast {

    }

    override fun forType(objectTypeDSL: ObjectTypeDSL, block: TypeScope.() -> Unit) {
        val ocScopeImpl = OCScopeImpl(
            rootScope = rootScope ?: this,
            scopeType = objectTypeDSL,
            scopeEntities = ScopeAccessibleEntities(
                groupsIdCreator = groupsIdCreator,
                parentScopeEntities = ScopeAccessibleEntities(
                    groupsIdCreator = groupsIdCreator,
                    parentScopeEntities = scopeEntities
                )
            ),
            groupsIdCreator = groupsIdCreator,
            arcDelegate = arcDelegate,
            placeDelegate = placeDelegate,
            transitionDelegate = transitionDelegate,
            objectTypeDelegate = objectTypeDelegate
        )
        ocScopeImpl.block()
    }

    companion object {
        const val DEFAULT_PLACE_PREFIX = "p"
        const val DEFAULT_TRANSITION_PREFIX = "t"
        const val DEFAULT_OBJECT_TYPE = "ot"
        const val USER_SAFE_PREFIX = "u"
    }
}
