package model

interface OCNetElements {
    val places: List<Place>

    val transitions: List<Transition>

    val arcs: List<Arc>

    val allPetriNodes: List<PetriNode>

    val objectTypes : List<ObjectType>
}
