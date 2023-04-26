package model

interface OCNetElements {
    val places: Places

    val transitions: Transitions

    val arcs: Arcs

    val allPetriNodes: List<PetriNode>

    val objectTypes : ObjectTypes

    val labelsActivities : LabelsActivities

    val placeTyping: PlaceTyping
}
