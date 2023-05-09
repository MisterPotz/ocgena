package model

interface OCNetElements {
    val places: Places

    val transitions: Transitions

    val arcs: Arcs

    val allPetriNodes: List<PetriNode>

    val objectTypes : ObjectTypes

    val placeTyping: PlaceTyping
}

typealias PlaceId = String

interface OCNetSimulationData {
    val inputPlaces : List<PlaceId>
    val outputPlaces : List<PlaceId>


}
