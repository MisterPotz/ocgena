package ru.misterpotz.ocgena.simulation_v2.entities

import ru.misterpotz.ocgena.simulation_old.ObjectType
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenGenerator
import java.util.*
import kotlin.collections.HashSet

sealed interface Token

class UngeneratedToken(val objectType: ObjectType) : Token {
    fun generate(tokenGenerator: TokenGenerator): TokenWrapper {
        return tokenGenerator.generateRealToken(objectType)
    }
}

class TokensEntry(val id: Long, val tokens: Set<TokenWrapper>)

class TokenHistory(
    private val entriesIds: MutableSet<Long>,
) : Comparable<TokenHistory>, Iterable<Long> {

    companion object {
        val comparator = compareBy<TokenHistory>(
            {
                -it.size()
            },
            {
                it.min
            }
        )
    }

    var min: Long? = null
        private set
    var max: Long? = null
        private set

    fun add(entry: Long) {
        entriesIds.add(entry)
        min.let {
            if (it != null && entry < it) min = entry
            if (it == null) min = entry
        }
        max.let {
            if (it != null && entry > it) max = entry
            if (it == null) max = entry
        }
    }

    fun size(): Int = entriesIds.size

    override fun compareTo(other: TokenHistory): Int {
        return comparator.compare(this, other)
    }

    override fun iterator(): Iterator<Long> {
        return entriesIds.iterator()
    }
}

class TokenWrapper(
    val tokenId: Long,
    val objectType: ObjectType,
    tokenEntries : MutableSet<Long> = mutableSetOf()
) : Comparable<TokenWrapper>, Token {
    val tokenHistory = TokenHistory(tokenEntries)

    override fun compareTo(other: TokenWrapper): Int {
        return comparator.compare(this, other)
    }

    companion object {
        val comparator = compareBy<TokenWrapper>({
            it.tokenHistory
        }, {
            it.tokenId
        })
    }


    private val _visited = mutableSetOf<TransitionWrapper>()
    val visitedTransitions: Set<TransitionWrapper> = _visited

    fun recordTransitionVisit(transitionIndex: Long, transitionWrapper: TransitionWrapper) {
        _visited.add(transitionWrapper)
        tokenHistory.add(transitionIndex)
    }

    override fun toString(): String {
        return "${tokenId}[${objectType.id}]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TokenWrapper

        if (tokenId != other.tokenId) return false
        if (objectType != other.objectType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tokenId.hashCode()
        result = 31 * result + objectType.hashCode()
        return result
    }
}