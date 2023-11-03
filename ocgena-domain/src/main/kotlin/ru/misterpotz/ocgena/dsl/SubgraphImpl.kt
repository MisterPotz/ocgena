package dsl

import ru.misterpotz.ocgena.dsl.ArcDelegate

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
        subgraphConnectionResolver.resolveInputNode(linkChainDSL)
        return this
    }

    override fun connectToLeftOf(linkChainDSL: LinkChainDSL): HasLast {
        subgraphConnectionResolver.resolveOutputNode(linkChainDSL)
        return HasLastImpl(linkChainDSL.lastElement)
    }
}
