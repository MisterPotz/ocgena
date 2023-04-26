package model

import utils.print


class ObjectMarking(val placesToObjectTokens: MutableMap<PlaceId, MutableSet<ObjectToken>> = mutableMapOf()) {
    operator fun get(place: PlaceId): MutableSet<ObjectToken>? {
        return placesToObjectTokens[place]
    }

    operator fun get(place: Place): MutableSet<ObjectToken>? {
        return placesToObjectTokens[place.id]
    }

    fun shiftTokenTime(tokenTimeDelta: Time) {
        allTokens().forEach {
            it.ownPathTime += tokenTimeDelta
        }
    }

    operator fun set(place: PlaceId, set: Set<ObjectToken>) {
        placesToObjectTokens[place] = set.toMutableSet()
    }

    operator fun set(place: Place, set: Set<ObjectToken>) {
        placesToObjectTokens[place.id] = set.toMutableSet()
    }

    fun setTokens(place: PlaceId, tokens: Collection<ObjectToken>) {
        placesToObjectTokens[place] = placesToObjectTokens[place].let {
            it?.apply { addAll(tokens) } ?: mutableSetOf<ObjectToken>().apply { addAll(tokens) }
        }
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

    operator fun plusAssign(objectMarking: ImmutableObjectMarking) {
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

    operator fun minusAssign(objectMarking: ImmutableObjectMarking) {
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

        val newMap = mutableMapOf<PlaceId, MutableSet<ObjectToken>>()

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

        val newMap = mutableMapOf<PlaceId, MutableSet<ObjectToken>>()

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
        return placesToObjectTokens.keys.joinToString(separator = "\n") { place ->
            val objectTokens = placesToObjectTokens[place]!!

            val objectTokensString = objectTokens.joinToString(separator = " ") { "${it.name}[${it.ownPathTime.print()}]" }
            """${place}: $objectTokensString"""
        }
    }

    companion object {
        fun build(block: MutableMap<PlaceId, MutableSet<ObjectToken>>.() -> Unit) : ObjectMarking {
            val map = mutableMapOf<PlaceId, MutableSet<ObjectToken>>()
            map.block()
            val newObjectMarking = ObjectMarking(map)
            return newObjectMarking
        }
    }
}
