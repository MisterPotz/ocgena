package dsl

class SubgraphArcDelegate(
    arcContainer: ArcContainer,
    val inNode : HasLast,
    val outNode : HasFirst,
    val inputNodeDependentCommands: MutableList<() -> Unit> = mutableListOf(),
    val outputNodeDependentCommands : MutableList<() -> Unit> = mutableListOf()
) : ArcDelegate(arcContainer) {
    private val arcCreator = ArcCreator()

    private fun addOnOutputNodeArcCommands(
        multiplicity: Int,
        hasFirst: HasFirst,
        variable: Boolean
    ) {
        outputNodeDependentCommands.add {
            if (variable) {
                inNode.variableArcTo(hasFirst)
            } else {
                inNode.arcTo(multiplicity, hasFirst)
            }
        }
    }

    private fun addOnInputNodeArcCommands(
        multiplicity: Int,
        hasFirst: HasFirst,
        variable: Boolean
    ) {
        inputNodeDependentCommands.add {
            if (variable) {
                inNode.variableArcTo(hasFirst)
            } else {
                inNode.arcTo(multiplicity, hasFirst)
            }
        }
    }

    private fun createAndAddArc(
        from: HasLast,
        to: HasFirst,
        multiplicity: Int,
        isVariable: Boolean,
    ) {
        val newArc = arcCreator.createArc(from, to, multiplicity, isVariable)
        if (from == inNode && to == outNode) throw IllegalStateException()

        if (from == inNode) {
            addOnInputNodeArcCommands(multiplicity = multiplicity, variable = isVariable, hasFirst = to)
        }
        if (to == outNode) {
            addOnOutputNodeArcCommands(multiplicity = multiplicity, variable = isVariable, hasFirst = to)
        }
        arcContainer.arcs.add(newArc)
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
