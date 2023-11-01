package model

import kotlinx.serialization.Serializable

@Serializable
data class SerializableCoreOcNet(
    val inputPlaces: List<SerializablePlace>,
    val outputPlaces: List<SerializablePlace>,
    val allPlaces : List<SerializablePlace>,
    val transitions : List<SerializableTransition>,
    val objectTypes : List<ObjectType>,
    val arcs : List<SerializableAtom>,
    val placeTyping : SerializablePlaceTyping,
)

/**
 * the net, formed with passed arguments, must already be consistent
 */
class StaticCoreOcNet(
    val inputPlaces: Places,
    val outputPlaces: Places,
    override val allArcs : List<Arc>,
    override val places: Places,
    override val transitions: Transitions,
    override val objectTypes: ObjectTypes,
    override val arcs: Arcs,
    override val allPetriNodes: List<PetriNode>,
    override val placeTyping: PlaceTyping,
) : OCNetElements {
    override fun toString(): String {
        return "OCNet(inputPlaces=$inputPlaces, outputPlaces=$outputPlaces, objectTypes=$objectTypes)"
    }

    fun dumpSerializable() : SerializableCoreOcNet {
        return SerializableCoreOcNet(
            inputPlaces = inputPlaces.map { it.serializableAtom },
            outputPlaces = outputPlaces.map { it.serializableAtom },
            allPlaces = places.map { it.serializableAtom },
            transitions = transitions.map { it.serializableAtom },
            objectTypes = objectTypes,
            arcs = allArcs.map { it.serializableAtom },
            placeTyping = placeTyping.toSerializable()
        )
    }

    private val labelToNode: MutableMap<String, PetriNode> = mutableMapOf()
}

