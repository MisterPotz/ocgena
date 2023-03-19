package dsl

class ArcCreator(
) {
    fun createArc(
        from: HasElement,
        to: HasElement,
        multiplicity: Int,
        isVariable: Boolean,
    ): ArcDSL {
        val fromElement = when (from) {
            is HasLast -> from.lastElement
            else -> from.element
        }
        val toElement = when(to) {
            is HasFirst -> to.firstElement
            else -> to.element
        }

        val newArc = chooseArcTypeAndCreate(
            tailAtom = fromElement,
            arrowAtom = toElement,
            multiplicity = multiplicity,
            isVariable = isVariable
        )

        return newArc
    }


    private fun chooseArcTypeAndCreate(
        tailAtom: NodeDSL,
        arrowAtom: NodeDSL,
        multiplicity: Int,
        isVariable: Boolean,
    ): ArcDSL {
        return if (isVariable) {
            VariableArcDSLImpl(tailAtom = tailAtom, arrowAtom = arrowAtom)
        } else {
            NormalArcDSLImpl(multiplicity = multiplicity, tailAtom = tailAtom, arrowAtom = arrowAtom)
        }
    }
}
