package model

class ObjectMarking(private val placesToObjectTokens: MutableMap<Place, MutableSet<ObjectToken>> = mutableMapOf()) {
    operator fun get(place: Place): Set<ObjectToken>? {
        return placesToObjectTokens[place]
    }

    operator fun set(place: Place, set: Set<ObjectToken>) {
        placesToObjectTokens[place] = set.toMutableSet()
    }

    fun setTokens(place: Place, tokens: Collection<ObjectToken>) {
        placesToObjectTokens[place] = placesToObjectTokens[place].let {
            it?.apply { addAll(tokens) } ?: mutableSetOf<ObjectToken>().apply { addAll(tokens) }
        }
    }

    fun nonEmptyPlaces() : Collection<Place> {
        return placesToObjectTokens.keys
    }

    fun allTokens() : Collection<ObjectToken> {
        return placesToObjectTokens.values.fold(mutableSetOf()) { accum, set ->
            accum.addAll(set)
            accum
        }
    }

    fun copy(): ObjectMarking {
        return ObjectMarking(buildMap {
            placesToObjectTokens.forEach {
                put(it.key, it.value)
            }
        }.toMutableMap())
    }

    operator fun plusAssign(objectMarking: ObjectMarking) {
        val addedKeys = objectMarking.placesToObjectTokens.keys

        for (place in addedKeys) {
            val current = placesToObjectTokens[place] ?: setOf()
            val added = objectMarking[place] ?: setOf()

            val newSet = current + added
            if (newSet.isNotEmpty()) {
                placesToObjectTokens[place] = newSet.toMutableSet()
            }
        }
    }

    operator fun minusAssign(objectMarking: ObjectMarking) {
        val subtractedKeys = objectMarking.placesToObjectTokens.keys

        for (place in subtractedKeys) {
            val current = placesToObjectTokens[place] ?: setOf()
            val subtracted = objectMarking[place] ?: setOf()

            val newSet = current - subtracted
            if (newSet.isNotEmpty()) {
                placesToObjectTokens[place] = newSet.toMutableSet()
            }
        }
    }

    operator fun minus(objectMarking: ObjectMarking): ObjectMarking {
        val subtractedKeys = objectMarking.placesToObjectTokens.keys

        val newMap = mutableMapOf<Place, MutableSet<ObjectToken>>()

        for (place in subtractedKeys) {
            val current = placesToObjectTokens[place] ?: setOf()
            val subtracted = objectMarking[place] ?: setOf()

            val newSet = current - subtracted
            if (newSet.isNotEmpty()) {
                newMap[place] = newSet.toMutableSet()
            }
        }

        return ObjectMarking(newMap)
    }

    operator fun plus(objectMarking: ObjectMarking): ObjectMarking {
        val addedKeys = objectMarking.placesToObjectTokens.keys

        val newMap = mutableMapOf<Place, MutableSet<ObjectToken>>()

        for (place in addedKeys) {
            val current = placesToObjectTokens[place] ?: setOf()
            val added = objectMarking[place] ?: setOf()

            val newSet = current + added
            if (newSet.isNotEmpty()) {
                newMap[place] = newSet.toMutableSet()
            }
        }

        return ObjectMarking(newMap)
    }
}
