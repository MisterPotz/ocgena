package model

import ru.misterpotz.model.atoms.Arc

interface OCNetElements {
    val places: Places

    val transitions: Transitions

    val arcs: Arcs

    val allArcs : List<Arc>

    val allPetriNodes: List<PetriNode>

    val objectTypes : ObjectTypes

    val placeTyping: PlaceTyping
}

typealias PlaceId = String

interface OCNetSimulationData {
    val inputPlaces : List<PlaceId>
    val outputPlaces : List<PlaceId>


}
