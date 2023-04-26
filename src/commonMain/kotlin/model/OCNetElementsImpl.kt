package model

class OCNetElementsImpl(
    override val places : Places,
    override val transitions: Transitions,
    override val arcs : Arcs,
    override val objectTypes: ObjectTypes
): OCNetElements {
    override val allPetriNodes : List<PetriNode> = buildList {
        addAll(places)
        addAll(transitions)
    }
    override val labelsActivities: LabelsActivities = LabelsActivities.createFromTransitions(transitions)
    override val placeTyping: PlaceTyping = PlaceTyping()

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

