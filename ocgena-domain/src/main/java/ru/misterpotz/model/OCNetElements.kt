package model

import ru.misterpotz.model.atoms.Arc
import ru.misterpotz.model.collections.ObjectTypes

interface OCNetElements {
    val places: Places

    val transitions: Transitions

    val arcs: Arcs

    val objectTypes : ObjectTypes

    val placeTyping: PlaceTyping
}

typealias PlaceId = String

interface OCNetSimulationData {
    val inputPlaces : List<PlaceId>
    val outputPlaces : List<PlaceId>


}
