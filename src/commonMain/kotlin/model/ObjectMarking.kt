package model

data class ObjectMarking(private val placesToObjectTokens: MutableMap<Place, MutableSet<ObjectToken>> = mutableMapOf()) {
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
            placesToObjectTokens[place] = newSet.toMutableSet()
        }
    }

    operator fun minusAssign(objectMarking: ObjectMarking) {
        val subtractedKeys = objectMarking.placesToObjectTokens.keys

        for (place in subtractedKeys) {
            val current = placesToObjectTokens[place] ?: setOf()
            val subtracted = objectMarking[place] ?: setOf()

            val newSet = current - subtracted
            placesToObjectTokens[place] = newSet.toMutableSet()
        }
    }

    operator fun minus(objectMarking: ObjectMarking): ObjectMarking {
        val subtractedKeys = objectMarking.placesToObjectTokens.keys

        val newMap = mutableMapOf<Place, MutableSet<ObjectToken>>()

        for (place in subtractedKeys) {
            val current = placesToObjectTokens[place] ?: setOf()
            val subtracted = objectMarking[place] ?: setOf()

            val newSet = current - subtracted
            newMap[place] = newSet.toMutableSet()
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
            newMap[place] = newSet.toMutableSet()
        }

        return ObjectMarking(newMap)
    }

    fun prettyPrint() : String {
        return placesToObjectTokens.entries.fold(StringBuilder()) { accum, line ->
            accum.append(line.key)
            accum.append(" |\n")
            accum.append(line.value.joinToString(separator = "\n") {
                it.name
            }.prependIndent(" "))
            accum.append('\n')
            accum
        }.toString()
    }

    private var stringBuilder = StringBuilder()
    override fun toString(): String {
        stringBuilder.clear()
        placesToObjectTokens.forEach { entry ->
            val place = entry.key
            val objectTokens = entry.value
            stringBuilder.append("""${place.id}: ${
                objectTokens.joinToString(separator = " ") { it.name }
            }
            """.trimMargin())
            stringBuilder.append("\n")
        }
        return stringBuilder.toString().also {
            stringBuilder.clear()
        }
    }

    companion object {
        fun build(block: MutableMap<Place, MutableSet<ObjectToken>>.() -> Unit) : ObjectMarking {
            val map = mutableMapOf<Place, MutableSet<ObjectToken>>()
            map.block()
            val newObjectMarking = ObjectMarking(map)
            return newObjectMarking
        }
    }
}
