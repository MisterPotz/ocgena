package ru.misterpotz.marking.plain

import model.PlaceId

class PlainMarking {
    private val placesToTokens: MutableMap<PlaceId, Int> = mutableMapOf()

    fun allPlaces() : Collection<PlaceId> {
        return placesToTokens.keys
    }

    operator fun get(place : PlaceId) : Int {
        return placesToTokens[place] ?: 0
    }

    companion object {

        fun of(map: MutableMap<PlaceId, Int>.() -> Unit): PlainMarking {
            return PlainMarking().apply {
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