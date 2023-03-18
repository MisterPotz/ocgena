package dsl

class SubgraphImpl(
    override val label: String,
    val rootScope: OCScopeImpl,
    val inNodeSpec: UnresolvedHasLast = UnresolvedHasLast(),
    val outNodeSpec: UnresolvedHasFirst= UnresolvedHasFirst(),
    val inputNodeDependentCommands: MutableList<() -> Unit> = mutableListOf(),
    val outputNodeDependentCommands : MutableList<() -> Unit> = mutableListOf(),
    private val placeDelegate: PlaceDelegate,
    private val transitionDelegate: TransitionDelegate,
    private val arcDelegate: ArcDelegate,
) : SubgraphDSL,
    PlaceAcceptor by placeDelegate,
    TransitionAcceptor by transitionDelegate,
    ArcsAcceptor by rootScope,
    SubgraphConnector {
    val subgraphStruct = EntitiesCreatedInSubgraph()

    override val inNode: HasLast
        get() = inNodeSpec
    override val outNode: HasFirst
        get() = outNodeSpec

    override fun subgraph(label: String?, block: SubgraphDSL.() -> Unit): SubgraphDSL {
        val subgraphDSL = rootScope.internalCreateSubgraph(label, block)
        subgraphStruct.subgraphs[subgraphDSL.label] = subgraphDSL
        return subgraphDSL
    }

    override fun SubgraphDSL.connectTo(linkChainDSL: LinkChainDSL): HasLast {
        return if (this is SubgraphImpl) {
            this.internalConnectTo(linkChainDSL)
        } else throw IllegalStateException()
    }

    override fun LinkChainDSL.connectTo(subgraphDSL: SubgraphDSL): SubgraphDSL {
       return if (subgraphDSL is SubgraphImpl) {
           subgraphDSL.internalPreconnectWith(this)
       } else throw IllegalStateException()
    }

    private fun internalPreconnectWith(linkChainDSL: LinkChainDSL): SubgraphDSL {
        val last = linkChainDSL.lastElement
        inNodeSpec.resolvedLastElement = last
        for (inputNodeCommand in inputNodeDependentCommands) {
            inputNodeCommand.invoke()
        }
        return this
    }

    private fun internalConnectTo(linkChainDSL: LinkChainDSL): HasLast {
        val first = linkChainDSL.firstElement
        outNodeSpec.resolvedFirstElement = first
        for (outputNodeCommand in outputNodeDependentCommands) {
            outputNodeCommand.invoke()
        }
        return HasLastImpl(linkChainDSL.lastElement)
    }
}
