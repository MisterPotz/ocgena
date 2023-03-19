package dsl

class SubgraphConnectionResolver(
    val inNode : UnresolvedHasLast = UnresolvedHasLast(),
    val outNode : UnresolvedHasFirst = UnresolvedHasFirst(),
    private val inputNodeDependentCommands: MutableList<(inNode : HasLast) -> Unit> = mutableListOf(),
    private val outputNodeDependentCommands : MutableList<(outNode : HasFirst) -> Unit> = mutableListOf()
) {
    fun checkNodeConnectionIsUnresolved(
        fromNode : HasLast,
        toNode : HasFirst) : Boolean {
        if (fromNode == inNode && toNode == outNode) throw IllegalStateException()

        if (fromNode == inNode) {
            return true
        }
        if (toNode == outNode) {
            return true
        }
        return false
    }
    fun checkIfInputNodeUnresolvedAndAddCommand(fromNode: HasLast, command: (inNode: HasLast) -> Unit) {
        if (fromNode == inNode) {
            inputNodeDependentCommands.add(command)
        }
    }
    fun checkIfOutputNodeUnresolvedAndAddCommand(toNode: HasFirst, command: (outNode: HasFirst) -> Unit) {
        if (outNode == toNode) {
            outputNodeDependentCommands.add(command)
        }
    }

    fun resolveInputNode(fromNode: HasLast) {
        inNode.resolvedLastElement = fromNode.lastElement
        for (inputNodeCommand in inputNodeDependentCommands) {
            inputNodeCommand.invoke(inNode)
        }
    }

    fun resolveOutputNode(toNode : HasFirst) {
        outNode.resolvedFirstElement = toNode.firstElement
        for (outputNodeCommand in outputNodeDependentCommands) {
            outputNodeCommand.invoke(outNode)
        }
    }
}

class SubgraphArcDelegate(
    private val subgraphConnectionResolver: SubgraphConnectionResolver,
    arcContainer: ArcContainer,
) : ArcDelegate(arcContainer) {
    private val arcCreator = ArcCreator()

    private fun createAndAddArc(
        from: HasLast,
        to: HasFirst,
        multiplicity: Int,
        isVariable: Boolean,
    ) {
        if (subgraphConnectionResolver.checkNodeConnectionIsUnresolved(from, to)) {
            subgraphConnectionResolver.checkIfInputNodeUnresolvedAndAddCommand(fromNode = from) { inNode ->
                if (isVariable) {
                    inNode.variableArcTo(to)
                } else {
                    inNode.arcTo(multiplicity, to)
                }
            }
            subgraphConnectionResolver.checkIfOutputNodeUnresolvedAndAddCommand(toNode = to) { outNode ->
                if (isVariable) {
                    from.variableArcTo(outNode)
                } else {
                    from.arcTo(multiplicity, outNode)
                }
            }
        } else {
            val newArc = arcCreator.createArc(from, to, multiplicity, isVariable)
            arcContainer.arcs.add(newArc)
        }
    }

    override fun HasLast.arcTo(multiplicity: Int, linkChainDSL: LinkChainDSL): HasLast {
        createAndAddArc(this, linkChainDSL, multiplicity, isVariable = false)
        return HasLastImpl(linkChainDSL.lastElement)
    }

    override fun HasLast.arcTo(multiplicity: Int, linkChainDSL: HasFirst) {
        createAndAddArc(this, linkChainDSL, multiplicity, isVariable = false)
    }

    override fun HasLast.variableArcTo(linkChainDSL: LinkChainDSL): HasLast {
        createAndAddArc(this, linkChainDSL, multiplicity = 1, isVariable = true)
        return HasLastImpl(linkChainDSL.lastElement)
    }

    override fun HasLast.variableArcTo(hasFirst: HasFirst) {
        createAndAddArc(this, hasFirst, multiplicity = 1, isVariable = true)
    }

    override fun LinkChainDSL.arcTo(multiplicity: Int, linkChainDSL: LinkChainDSL): LinkChainDSL {
        createAndAddArc(this, linkChainDSL, multiplicity, isVariable = false)
        return LinkChainDSLImpl(firstElement = this.firstElement, lastElement = linkChainDSL.lastElement)
    }

    override fun LinkChainDSL.variableArcTo(linkChainDSL: LinkChainDSL): LinkChainDSL {
        createAndAddArc(this, linkChainDSL, multiplicity = 1, isVariable = true)
        return LinkChainDSLImpl(firstElement = this.firstElement, lastElement = linkChainDSL.lastElement)
    }
}
