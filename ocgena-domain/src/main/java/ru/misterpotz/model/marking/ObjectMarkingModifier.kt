package ru.misterpotz.model.marking

import model.PlaceId

interface ObjectMarkingModifier {
    fun applyTo(map: MutableMap<PlaceId, MutableSet<ObjectTokenId>>)
}

fun buildObjectMarkingModifier(
    block: (modificationTarget: MutableMap<PlaceId, MutableSet<ObjectTokenId>>) -> Unit
): ObjectMarkingModifier {
    return object : ObjectMarkingModifier {
        override fun applyTo(map: MutableMap<PlaceId, MutableSet<ObjectTokenId>>) {
            block(map)
        }
    }
}