package ru.misterpotz.ocgena.simulation_v2

import ru.misterpotz.ocgena.simulation_v2.entities_selection.ModelAccessor
import ru.misterpotz.ocgena.simulation_v2.entities_storage.SimpleTokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice

fun SimpleTokenSlice.copyFromMap(
    model: ModelAccessor,
    map: Map<String, List<Int>>
): TokenSlice {
    return SimpleTokenSlice.of(
        buildMap {
            for ((place, tokens) in map) {
                put(model.place(place), tokens.map { tokenBy(it.toString()) })
            }
        }
    )
}