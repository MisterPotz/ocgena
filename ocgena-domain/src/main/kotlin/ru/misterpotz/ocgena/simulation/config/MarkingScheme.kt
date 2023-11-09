package ru.misterpotz.ocgena.simulation.config

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId

@Serializable
data class MarkingScheme(val placesToTokens: Map<PetriAtomId, Int> = mutableMapOf()) {

    fun allPlaces(): Collection<PetriAtomId> {
        return placesToTokens.keys
    }

    operator fun get(place: PetriAtomId): Int {
        return placesToTokens[place] ?: 0
    }

    companion object {

        fun of(map: MutableMap<PetriAtomId, Int>.() -> Unit): MarkingScheme {
            val map = buildMap {
                map()
            }
            return MarkingScheme(map)
        }
    }
}
