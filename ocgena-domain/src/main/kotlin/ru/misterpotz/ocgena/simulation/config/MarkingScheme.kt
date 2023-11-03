package ru.misterpotz.ocgena.simulation.config

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId

@Serializable
data class MarkingScheme(private val placesToTokens: MutableMap<PetriAtomId, Int> = mutableMapOf()) {

    fun allPlaces(): Collection<PetriAtomId> {
        return placesToTokens.keys
    }

    operator fun get(place: PetriAtomId): Int {
        return placesToTokens[place] ?: 0
    }

    companion object {

        fun of(map: MutableMap<PetriAtomId, Int>.() -> Unit): MarkingScheme {
            return MarkingScheme().apply {
                placesToTokens.putAll(
                    buildMap {
                        map()
                    }.toList().fold(mutableMapOf()) { accum, entry ->
                        accum[entry.first] = entry.second
                        accum
                    }
                )
            }
        }
    }
}
