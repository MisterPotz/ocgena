package model


class OCNetElementsImpl(
    override val places : Places,
    override val transitions: Transitions,
    override val arcs : Arcs,
    override val objectTypes: ObjectTypes,
    override val placeTyping: PlaceTyping,
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

