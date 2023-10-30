package dsl

class ArcCreator(private val arcContainer: ArcContainer) {
    private fun countOutputArcsForNode(nodeDSL: NodeDSL) : Int {
        return arcContainer.arcs.count {
            it.isOutputFor(nodeDSL)
        }
    }
    fun createArc(
        from: HasElement,
        to: HasElement,
        arcTypeCreateParameters: TypedArcCreator,
    ): ArcDSL {
        val fromElement = when (from) {
            is HasLast -> from.lastElement
            else -> from.element
        }
        val toElement = when (to) {
            is HasFirst -> to.firstElement
            else -> to.element
        }

        return arcTypeCreateParameters.create(
            TypedArcCreator.ArcCreationDependencies(
                tailAtom = fromElement,
                arrowAtom = toElement,
                arcIndexForTailAtom = countOutputArcsForNode(fromElement)
            )
        )
    }
}