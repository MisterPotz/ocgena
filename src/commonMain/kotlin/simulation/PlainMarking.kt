package simulation

import model.Place
import model.StaticCoreOcNet


typealias ObjectsMap = List<ObjectTokenReference>
typealias ObjectTokenReference = String
typealias ValuesMap = Map<String, Any>
typealias Timestamp = String

class PlainMarking {
    private val placesToTokens: MutableMap<Place, Int> = mutableMapOf()

    fun allPlaces() : Collection<Place> {
        return placesToTokens.keys
    }

    operator fun get(place : Place) : Int {
        return placesToTokens[place] ?: 0
    }

    companion object {
        fun of(map: MutableMap<Place, Int>): PlainMarking {
            return PlainMarking().apply {
                placesToTokens.putAll(map)
            }
        }

        fun of(map: MutableMap<Place, Int>.() -> Unit): PlainMarking {
            return PlainMarking().apply {
                placesToTokens.putAll(
                    buildMap {
                        map()
                    }
                )
            }
        }
    }
}
