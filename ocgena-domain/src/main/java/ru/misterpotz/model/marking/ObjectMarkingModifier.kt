package ru.misterpotz.model.marking

import model.PlaceId
import java.util.SortedSet

interface ObjectMarkingModifier {
    fun applyTo(map: MutableMap<PlaceId, SortedSet<ObjectTokenId>>)
}

fun buildObjectMarkingModifier(
    block: (modificationTarget: MutableMap<PlaceId, SortedSet<ObjectTokenId>>) -> Unit
): ObjectMarkingModifier {
    return object : ObjectMarkingModifier {
        override fun applyTo(map: MutableMap<PlaceId, SortedSet<ObjectTokenId>>) {
            block(map)
        }
    }
}