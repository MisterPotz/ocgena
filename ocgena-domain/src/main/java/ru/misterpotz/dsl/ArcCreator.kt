package dsl

sealed class TypedArcCreator {
    data class NormalArc(val multiplicity: Int) : TypedArcCreator() {
        fun create(tailAtom: NodeDSL, arrowAtom: NodeDSL): ArcDSL {
            return NormalArcDSLImpl(
                multiplicity = multiplicity,
                tailAtom = tailAtom,
                arcIndexForTailAtom = 0,
                arrowAtom = arrowAtom
            )
        }

        override fun create(arcCreationDependencies: ArcCreationDependencies): ArcDSL {
            return with(arcCreationDependencies) {
                NormalArcDSLImpl(
                    multiplicity = multiplicity,
                    tailAtom = tailAtom,
                    arcIndexForTailAtom = arcIndexForTailAtom,
                    arrowAtom = arrowAtom
                )
            }
        }
    }

    object VariableArc : TypedArcCreator() {
        fun create(tailAtom: NodeDSL, arrowAtom: NodeDSL): ArcDSL {
            return VariableArcDSLImpl(tailAtom = tailAtom, arrowAtom = arrowAtom, arcIndexForTailAtom = 0)
        }

        override fun create(arcCreationDependencies: ArcCreationDependencies): ArcDSL {
            return with(arcCreationDependencies) {
                VariableArcDSLImpl(
                    tailAtom = tailAtom,
                    arcIndexForTailAtom = arcIndexForTailAtom,
                    arrowAtom = arrowAtom
                )
            }
        }
    }

    class ArcCreationDependencies(
        val tailAtom: NodeDSL,
        val arrowAtom: NodeDSL,
        val arcIndexForTailAtom: Int,
    )

    abstract fun create(arcCreationDependencies: ArcCreationDependencies): ArcDSL

//    abstract fun create(
//        tailAtom: NodeDSL,
//        arrowAtom: NodeDSL,
//    ): ArcDSL
}

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