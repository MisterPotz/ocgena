package model

import kotlinx.serialization.Serializable
import ru.misterpotz.model.collections.ObjectTypes

/**
 * the net, formed with passed arguments, must already be consistent
 */
@Serializable
data class StaticCoreOcNet(
    val inputPlaces: Places,
    val outputPlaces: Places,
    override val places: Places,
    override val transitions: Transitions,
    override val objectTypes: ObjectTypes,
    override val arcs: Arcs,
    override val placeTyping: PlaceTyping,
) : OCNetElements {
    override fun toString(): String {
        return "OCNet(inputPlaces=$inputPlaces, outputPlaces=$outputPlaces, objectTypes=$objectTypes)"
    }
}

