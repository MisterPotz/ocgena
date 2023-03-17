package model

import converter.OCNetElements

class OCNetElementsImpl(
    override val places : List<Place>,
    override val transitions: List<Transition>,
    override val arcs : List<Arc>,
    override val allPetriNodes : List<PetriNode>
): OCNetElements {
    override fun toString(): String {
        return """
            |Output(
            |   places: $places
            |   transitions: $transitions,
            |   arcs: $arcs
            |)
        """.trimMargin()
    }
}
