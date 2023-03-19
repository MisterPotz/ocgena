package dsl

open class ArcDelegate(
    val arcContainer: ArcContainer,
) : ArcsAcceptor {
    private val arcCreator = ArcCreator()

    private fun createAndAddArc(
        from: HasElement,
        to: HasElement,
        multiplicity: Int,
        isVariable: Boolean,
    ) {
        val newArc = arcCreator.createArc(from, to, multiplicity, isVariable)
        arcContainer.arcs.add(newArc)
    }

    override fun HasElement.arcTo(multiplicity: Int, linkChainDSL: LinkChainDSL): HasLast {
        createAndAddArc(this, linkChainDSL, multiplicity, isVariable = false)
        return HasLastImpl(linkChainDSL.lastElement)
    }

    override fun HasElement.arcTo(multiplicity: Int, linkChainDSL: HasElement) {
        createAndAddArc(this, linkChainDSL, multiplicity, isVariable = false)
    }

    override fun HasElement.variableArcTo(linkChainDSL: LinkChainDSL): HasLast {
        createAndAddArc(this, linkChainDSL, multiplicity = 1, isVariable = true)
        return HasLastImpl(linkChainDSL.lastElement)
    }

    override fun HasElement.variableArcTo(hasFirst: HasElement) {
        createAndAddArc(this, hasFirst, multiplicity = 1, isVariable = true)
    }
}

