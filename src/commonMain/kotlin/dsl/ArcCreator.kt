package dsl

sealed class TypedArcCreator {
    data class NormalArc(val multiplicity: Int) : TypedArcCreator() {
        override fun create(tailAtom: NodeDSL, arrowAtom: NodeDSL): ArcDSL {
            return NormalArcDSLImpl(multiplicity = multiplicity, tailAtom = tailAtom, arrowAtom = arrowAtom)
        }
    }

    object VariableArc : TypedArcCreator() {
        override fun create(tailAtom: NodeDSL, arrowAtom: NodeDSL): ArcDSL {
            return VariableArcDSLImpl(tailAtom = tailAtom, arrowAtom = arrowAtom)
        }
    }

    abstract fun create(tailAtom: NodeDSL, arrowAtom: NodeDSL): ArcDSL
}

class ArcCreator(
) {
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

        return arcTypeCreateParameters.create(tailAtom = fromElement, arrowAtom = toElement)
    }
}
