package dsl

import ru.misterpotz.ocgena.dsl.ArcContainer
import ru.misterpotz.ocgena.dsl.ArcCreator
import ru.misterpotz.ocgena.dsl.ArcDelegate
import ru.misterpotz.ocgena.dsl.TypedArcCreator

class SubgraphConnectionResolver(
    val inNode: UnresolvedHasLast = UnresolvedHasLast(),
    val outNode: UnresolvedHasFirst = UnresolvedHasFirst(),
    private val inputNodeDependentCommands: MutableList<(inNode: HasLast) -> Unit> = mutableListOf(),
    private val outputNodeDependentCommands: MutableList<(outNode: HasFirst) -> Unit> = mutableListOf(),
) {
    fun checkNodeConnectionIsUnresolved(
        fromNode: HasElement,
        toNode: HasElement,
    ): Boolean {
        if (fromNode == inNode && toNode == outNode) throw IllegalStateException()

        if (fromNode == inNode || toNode == inNode) {
            return true
        }
        if (toNode == outNode || toNode == outNode) {
            return true
        }
        return false
    }

    fun addOnInputNodeResolvedCommand(
        fromNode: HasElement,
        toNode: HasElement,
        command: (resolvedNode: HasElement) -> Unit,
    ) {
        require(checkNodeConnectionIsUnresolved(fromNode, toNode))
        when (inNode) {
            fromNode, toNode -> {
                inputNodeDependentCommands.add(command)
            }
        }
    }

    fun addOnOutputNodeResolvedCommand(
        fromNode: HasElement,
        toNode: HasElement,
        command: (resolvedNode: HasElement) -> Unit,
    ) {
        require(checkNodeConnectionIsUnresolved(fromNode, toNode))
        when (outNode) {
            fromNode, toNode -> {
                outputNodeDependentCommands.add(command)
            }
        }
    }

    fun resolveInputNode(fromNode: HasElement) {
        val element = fromNode.tryGetLastElement()
        inNode.resolvedLastElement = element
        for (inputNodeCommand in inputNodeDependentCommands) {
            inputNodeCommand.invoke(inNode)
        }
    }

    fun resolveOutputNode(toNode: HasElement) {
        val element = toNode.tryGetFirstElement()
        outNode.resolvedFirstElement = element
        for (outputNodeCommand in outputNodeDependentCommands) {
            outputNodeCommand.invoke(outNode)
        }
    }
}

class SubgraphArcDelegate(
    private val subgraphConnectionResolver: SubgraphConnectionResolver,
    arcContainer: ArcContainer,
) : ArcDelegate(arcContainer) {
    private val arcCreator = ArcCreator(arcContainer)


    private fun createAndAddPossiblyUnresolvedArc(
        from: HasElement,
        to: HasElement,
        typedArcCreator: TypedArcCreator,
    ) {
        if (subgraphConnectionResolver.checkNodeConnectionIsUnresolved(from, to)) {
            subgraphConnectionResolver.addOnInputNodeResolvedCommand(fromNode = from, toNode = to) { inNode ->
                createAndAddResolvedArc(from, to, typedArcCreator)
            }
            subgraphConnectionResolver.addOnOutputNodeResolvedCommand(fromNode = from, toNode = to) { outNode ->
                createAndAddResolvedArc(from, to, typedArcCreator)
            }
        } else {
            createAndAddResolvedArc(from, to, typedArcCreator)
        }
    }

    private fun createAndAddResolvedArc(from: HasElement, to: HasElement, typedArcCreator: TypedArcCreator) {
        super.createAndAddArc(from, to, typedArcCreator)
    }

    override fun HasElement.arcTo(linkChainDSL: LinkChainDSL, block: (NormalArcScope.() -> Unit)?): HasLast {
        val normalArcScope = createNormalArcScope(block)
        createAndAddPossiblyUnresolvedArc(
            this,
            linkChainDSL,
            TypedArcCreator.NormalArc(multiplicity = normalArcScope.multiplicity)
        )
        return HasLastImpl(linkChainDSL.lastElement)
    }

    override fun HasElement.arcTo(linkChainDSL: HasElement, block: (NormalArcScope.() -> Unit)?) {
        val normalArcScope = createNormalArcScope(block)
        createAndAddPossiblyUnresolvedArc(
            this,
            linkChainDSL,
            TypedArcCreator.NormalArc(multiplicity = normalArcScope.multiplicity)
        )
    }


    override fun HasElement.variableArcTo(linkChainDSL: LinkChainDSL): HasLast {
        createAndAddPossiblyUnresolvedArc(this, linkChainDSL, TypedArcCreator.VariableArc)
        return HasLastImpl(linkChainDSL.lastElement)
    }

    override fun HasElement.variableArcTo(hasFirst: HasElement) {
        createAndAddPossiblyUnresolvedArc(this, hasFirst, TypedArcCreator.VariableArc)
    }
}
