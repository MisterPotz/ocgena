package model

import eventlog.ObjectTypes
import ru.misterpotz.ocgena.registries.PlaceObjectTypeRegistry
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc


class OCNetElementsImpl(
    override val places : Places,
    override val transitionsRegistry: TransitionsRegistry,
    override val arcsRegistry : ArcsRegistry,
    override val objectTypes: ObjectTypes,
    override val placeObjectTypeRegistry: PlaceObjectTypeRegistry,
    val allArcs: List<Arc>,
): OCNetElements {

    override fun toString(): String {
        return """
            |Output(
            |   places: $places
            |   transitions: $transitionsRegistry,
            |   arcs: $arcsRegistry
            |)
        """.trimMargin()
    }
}

