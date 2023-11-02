package model

import ru.misterpotz.model.atoms.Arc
import ru.misterpotz.model.collections.ObjectTypes


class OCNetElementsImpl(
    override val places : Places,
    override val transitions: Transitions,
    override val arcs : Arcs,
    override val objectTypes: ObjectTypes,
    override val placeTyping: PlaceTyping,
    val allArcs: List<Arc>,
): OCNetElements {

    override fun toString(): String {
        return """
            |Output(
            |   places: $places
            |   transitions: $transitions,
            |   arcs: $arcs
            |)
        """.trimMargin()
    }
}

