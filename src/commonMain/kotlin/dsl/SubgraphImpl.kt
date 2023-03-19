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

    override val inNode: HasElement
        get() = subgraphConnectionResolver.inNode
    override val outNode: HasFirst
        get() = subgraphConnectionResolver.outNode

    override fun connectOnRightTo(linkChainDSL: HasElement): SubgraphDSL {

        resolveInputConnection(element)
        return internalConnectOnRightTo(linkChainDSL)
    }

    private fun resolveInputConnection(nodeDSL: NodeDSL) {
        subgraphConnectionResolver.resolveInputNode(linkChainDSL)

    }
    override fun connectToLeftOf(linkChainDSL: LinkChainDSL): HasLast {
        val element = linkChainDSL.firstElement

        return HasLastImpl(linkChainDSL.lastElement)
    }

    private fun internalConnectOnRightTo(linkChainDSL: HasElement): SubgraphDSL {
        return this
    }

    private fun internalConnectOnLeftOf(linkChainDSL: LinkChainDSL): HasLast {
        subgraphConnectionResolver.resolveOutputNode(linkChainDSL)
    }
}
