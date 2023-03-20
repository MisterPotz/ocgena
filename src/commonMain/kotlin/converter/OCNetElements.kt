package converter

import model.Arc
import model.ObjectType
import model.PetriNode
import model.Place
import model.Transition

interface OCNetElements {
    val places: List<Place>

    val transitions: List<Transition>

    val arcs: List<Arc>

    val allPetriNodes: List<PetriNode>

    val objectTypes : List<ObjectType>
}
