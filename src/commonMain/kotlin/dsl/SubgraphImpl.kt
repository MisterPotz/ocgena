package dsl

class SubgraphImpl(
    override val label: String,
    private val placeDelegate: PlaceDelegate,
    private val transitionDelegate: TransitionDelegate,
    private val arcDelegate: ArcDelegate,
    private val subgraphDelegate: SubgraphDelegate,
    private val subgraphConnectionResolver: SubgraphConnectionResolver
) : SubgraphDSL,
    PlaceAcceptor by placeDelegate,
    TransitionAcceptor by transitionDelegate,
    ArcsAcceptor by arcDelegate,
    SubgraphConnector by subgraphDelegate {

    override val inNode: HasLast
        get() = subgraphConnectionResolver.inNode
    override val outNode: HasFirst
        get() = subgraphConnectionResolver.outNode

    override fun leftConnectTo(linkChainDSL:LinkChainDSL): SubgraphDSL {
        return internalLeftConnect(linkChainDSL)
    }

    override fun rightConnectTo(linkChainDSL: LinkChainDSL): HasLast {
        return internalRightConnect(linkChainDSL)
    }

    private fun internalLeftConnect(linkChainDSL: LinkChainDSL): SubgraphDSL {
        subgraphConnectionResolver.resolveInputNode(linkChainDSL)
        return this
    }

    private fun internalRightConnect(linkChainDSL: LinkChainDSL): HasLast {
        subgraphConnectionResolver.resolveOutputNode(linkChainDSL)
        return HasLastImpl(linkChainDSL.lastElement)
    }
}
