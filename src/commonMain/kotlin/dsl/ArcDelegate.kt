package dsl


class NormalArcScopeImp : NormalArcScope {
    override var multiplicity: Int = 1
}

open class ArcDelegate(
    val arcContainer: ArcContainer,
) : ArcsAcceptor {
    private val arcCreator = ArcCreator()

    protected fun createAndAddArc(
        from: HasElement,
        to: HasElement,
        arcTypeCreateParameters: TypedArcCreator,
    ) {
        val newArc = arcCreator.createArc(from, to, arcTypeCreateParameters)
        arcContainer.arcs.add(newArc)
    }

    protected open fun createNormalArcScope(block: (NormalArcScope.() -> Unit)?): NormalArcScope {
        return NormalArcScopeImp().apply {
            if (block != null) block()
        }
    }

    override fun HasElement.arcTo(linkChainDSL: LinkChainDSL, block: (NormalArcScope.() -> Unit)?): HasLast {
        val normalArcScope = createNormalArcScope(block)
        createAndAddArc(
            this,
            linkChainDSL,
            TypedArcCreator.NormalArc(multiplicity = normalArcScope.multiplicity)
        )
        return HasLastImpl(linkChainDSL.lastElement)
    }

    override fun HasElement.arcTo(linkChainDSL: HasElement, block: (NormalArcScope.() -> Unit)?) {
        val normalArcScope = createNormalArcScope(block)
        createAndAddArc(this, linkChainDSL, TypedArcCreator.NormalArc(multiplicity = normalArcScope.multiplicity))
    }

    override fun HasElement.variableArcTo(linkChainDSL: LinkChainDSL): HasLast {
        createAndAddArc(this, linkChainDSL, TypedArcCreator.VariableArc)
        return HasLastImpl(linkChainDSL.lastElement)
    }

    override fun HasElement.variableArcTo(hasFirst: HasElement) {
        createAndAddArc(this, hasFirst, TypedArcCreator.VariableArc)
    }
}

