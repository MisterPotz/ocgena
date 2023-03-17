package dsl

class SubgraphImpl(
    override val label: String,
    val rootScope: OCScopeImpl,
) : SubgraphDSL, ArcsAcceptor by rootScope {
    val subgraphStruct = SubgraphStruct()

    override fun place(label: String): PlaceDSL {
        val newPlaceDSL = rootScope.place(label)
        subgraphStruct.places[newPlaceDSL.label] = newPlaceDSL
        return newPlaceDSL
    }

    override fun place(block: OCPlaceScope.() -> Unit): PlaceDSL {
        val newPlaceDSL = rootScope.place(block)
        subgraphStruct.places[newPlaceDSL.label] = newPlaceDSL
        return newPlaceDSL
    }

    override fun place(label: String, block: OCPlaceScope.() -> Unit): PlaceDSL {
        val newPlaceDSL = rootScope.place(label, block)
        subgraphStruct.places[newPlaceDSL.label] = newPlaceDSL
        return newPlaceDSL
    }

    override fun transition(block: OCTransitionScope.() -> Unit): TransitionDSL {
        val transitionDSL = rootScope.transition(block)
        subgraphStruct.transitions[transitionDSL.label] = transitionDSL
        return transitionDSL
    }

    override fun transition(label: String, block: OCTransitionScope.() -> Unit): TransitionDSL {
        val transitionDSL = rootScope.transition(label, block)
        subgraphStruct.transitions[transitionDSL.label] = transitionDSL
        return transitionDSL
    }

    override fun transition(label: String): TransitionDSL {
        val transitionDSL = rootScope.transition(label)
        subgraphStruct.transitions[transitionDSL.label] = transitionDSL
        return transitionDSL
    }

    override fun subgraph(label: String?, block: SubgraphDSL.() -> Unit): SubgraphDSL {
        val subgraphDSL = rootScope.internalCreateSubgraph(label, block)
        subgraphStruct.subgraphStructs[subgraphDSL.label] = subgraphDSL
        return subgraphDSL
    }

    override fun LinkChainDSL.connectTo(subgraphDSL: SubgraphDSL): SubgraphDSL {
        TODO("trigger subgraph internal connection to last element " +
                "of this node, to apply that element instead of unresolved one")
    }

    private val inputNodeDependentCommands: MutableList<() -> Unit> = mutableListOf()
    private val outputNodeDependentCommands : MutableList<() -> Unit> = mutableListOf()

    override fun HasLast.arcTo(multiplicity: Int, linkChainDSL: LinkChainDSL): HasLast {
        if (this is UnresolvedHasLast) {
            addOnINp

        }
        return HasLastImpl(linkChainDSL.lastElement)
    }

    private fun addOnInputNodeArcCommands(hasLast: UnresolvedHasLast, multiplicity: Int, hasFirst: HasFirst) {
        inputNodeDependentCommands.add {
            with(rootScope) {
                hasLast.arfcTo(multiplicity, hasFirst)
            }
        }
    }

    override fun LinkChainDSL.arcTo(multiplicity: Int, linkChainDSL: LinkChainDSL): LinkChainDSL {
        return with(rootScope) {
            this@arcTo.arcTo(multiplicity, linkChainDSL)
        }
    }

    override fun SubgraphDSL.connectTo(linkChainDSL: LinkChainDSL): HasLast {
        TODO("trigger execution of output node dependent commands here")
    }

    override fun HasLast.variableArcTo(linkChainDSL: LinkChainDSL): HasLast {
        inputNodeDependentCommands.add {

        }
    }

    override fun HasLast.variableArcTo(hasFirst: HasFirst) {
        TODO("Not yet implemented")
    }

    override fun LinkChainDSL.variableArcTo(linkChainDSL: LinkChainDSL): LinkChainDSL {
        TODO("Not yet implemented")
    }
}
