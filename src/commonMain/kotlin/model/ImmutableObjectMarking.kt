package model

import utils.print

class ImmutableObjectMarking(val placesToObjectTokens: Map<PlaceId, Set<ObjectToken>>) {
    fun toMutableObjectMarking() : ObjectMarking {
        return ObjectMarking(
            buildMap {
                for ((key, value) in placesToObjectTokens) {
                    put(key, value.toMutableSet())
                }
            }.toMutableMap()
        )
    }

    operator fun get(place: PlaceId): Set<ObjectToken>? {
        return placesToObjectTokens[place]
    }
    fun shiftTokenTime(tokenTimeDelta: Time) {
        allTokens().forEach {
            it.ownPathTime += tokenTimeDelta
        }
    }

    fun nonEmptyPlaces() : Collection<PlaceId> {
        return placesToObjectTokens.keys
    }

    fun allTokens() : Collection<ObjectToken> {
        return placesToObjectTokens.values.fold(mutableSetOf()) { accum, set ->
            accum.addAll(set)
            accum
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

    override fun toString(): String {
        return placesToObjectTokens.keys.joinToString(separator = "\n") { place ->
            val objectTokens = placesToObjectTokens[place]!!

            val objectTokensString = objectTokens.joinToString(separator = " ") { "${it.name}[${it.ownPathTime.print()}]" }
            """${place}: $objectTokensString"""
        }
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

    companion object {
        fun createFromObjectMarking(objectMarking: ObjectMarking) : ImmutableObjectMarking {
            return ImmutableObjectMarking(objectMarking.placesToObjectTokens)
        }
    }
}
