package dsl

class ArcCreator(
) {
    fun createArc(
        from: HasLast,
        to: HasFirst,
        multiplicity: Int,
        isVariable: Boolean,
    ): ArcDSL {
        val fromElement = from.lastElement
        val toElement = to.firstElement
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
