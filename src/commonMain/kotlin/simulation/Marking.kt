package simulation

import model.WellFormedOCNet

class Marking {
    private val placesToTokens: MutableMap<String, Int> = mutableMapOf()

    fun getWeakMarkingFor(place: String) : Int? {
        return placesToTokens[place]
    }

    fun getStrictMarkingFor(place: String): Int {
        return placesToTokens[place] ?: 0
    }

    fun setMarking(place: String, newMarking: Int) {
        placesToTokens[place] = newMarking
    }

    fun updateMarkingFrom(wellFormedOCNet: WellFormedOCNet) {
        for (i in wellFormedOCNet.places) {
            val tokens = i.tokens
            placesToTokens[i.label] = tokens
        }
    }

    companion object {
        fun of(map: MutableMap<String, Int>): Marking {
            return Marking().apply {
                placesToTokens.putAll(map)
            }
        }

        fun of(map: MutableMap<String, Int>.() -> Unit): Marking {
            return Marking().apply {
                placesToTokens.putAll(buildMap {
                    map()
                })
            }
        }
    }
}
