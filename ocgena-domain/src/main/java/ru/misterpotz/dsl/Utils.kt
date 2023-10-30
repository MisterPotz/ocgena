package dsl

object Utils {
    fun selectPlace(orderedAtomsList: List<AtomDSL>, block: PlaceDSL.(atomIndex: Int) -> Boolean): PlaceDSL {
        return orderedAtomsList
            .filterIsInstance<PlaceDSL>()
            .filterIndexed { index, atomDSL ->
                atomDSL.block(index)
            }.first()
    }

    fun selectTransition(
        orderedAtomsList: List<AtomDSL>,
        block: TransitionDSL.(atomIndex: Int) -> Boolean,
    ): TransitionDSL {
        return orderedAtomsList
            .filterIsInstance<TransitionDSL>()
            .filterIndexed { index, atomDSL ->
                atomDSL.block(index)
            }.first()
    }
}
