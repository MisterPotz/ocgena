package model

import eventlog.ObjectTypes
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.registries.PlaceObjectTypeRegistry

/**
 * the net, formed with passed arguments, must already be consistent
 */
@Serializable
data class StaticCoreOcNet(
    val inputPlaces: Places,
    val outputPlaces: Places,
    override val places: Places,
    override val transitionsRegistry: TransitionsRegistry,
    override val objectTypes: ObjectTypes,
    override val arcsRegistry: ArcsRegistry,
    override val placeObjectTypeRegistry: PlaceObjectTypeRegistry,
) : OCNetElements {
    override fun toString(): String {
        return "OCNet(inputPlaces=$inputPlaces, outputPlaces=$outputPlaces, objectTypes=$objectTypes)"
    }
}
