package dsl

class ArcDelegate(
    val arcs: MutableList<ArcDSL>
) : ArcsAcceptor {
    private val arcCreator = ArcCreator()

    private fun createAndAddArc(
        from: HasLast,
        to: HasFirst,
        multiplicity: Int,
        isVariable: Boolean,
    ) {
        val newArc = arcCreator.createArc(from, to, multiplicity, isVariable)
        arcs.add(newArc)
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
