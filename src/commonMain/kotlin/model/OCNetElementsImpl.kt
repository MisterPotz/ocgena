package model

class OCNetElementsImpl(
    override val places : List<Place>,
    override val transitions: List<Transition>,
    override val arcs : List<Arc>,
    override val objectTypes: List<ObjectType>
): OCNetElements {
    override val allPetriNodes : List<PetriNode> = buildList {
        addAll(places)
        addAll(transitions)
    }

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

