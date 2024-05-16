package ru.misterpotz.ocgena.simulation_v2.entities

import ru.misterpotz.ocgena.simulation_old.ObjectType
import java.util.*
import kotlin.collections.HashSet

class TokenWrapper(
    val tokenId: Long,
    val objectType: ObjectType
): Comparable<TokenWrapper> {

    override fun compareTo(other: TokenWrapper): Int {
        return tokenId.compareTo(other.tokenId)
    }

    private val _visited = mutableSetOf<TransitionWrapper>()
    val visitedTransitions: Set<TransitionWrapper> = _visited

    private val _participatedTransitionIndices = mutableMapOf<TransitionWrapper, SortedSet<Long>>()
    val participatedTransitionIndices: Map<TransitionWrapper, SortedSet<Long>> = _participatedTransitionIndices
    val allParticipatedTransitionEntries: HashSet<Long> = hashSetOf()

    fun participatedInAll(entries: HashSet<Long>) : Boolean {
        return entries.all { allParticipatedTransitionEntries.contains(it) }
    }

    fun hasSharedTransitionEntry(otherToken: TokenWrapper): Boolean {
        val haveSharedTransition = visitedTransitions.any { otherToken.visitedTransitions.contains(it) }
        if (!haveSharedTransition) return false

        val haveSharedTransitionEntry = participatedTransitionIndices.any { transitionEntries ->
            if (transitionEntries.key in otherToken.participatedTransitionIndices.keys) {
                val otherTokenEntries = otherToken.participatedTransitionIndices[transitionEntries.key]!!
                val thisEntry = transitionEntries.value
                val smallestEntryLog = if (otherTokenEntries.size < thisEntry.size) otherTokenEntries else thisEntry
                val biggestEntryLog = if (otherTokenEntries.size < thisEntry.size) thisEntry else otherTokenEntries

                smallestEntryLog.any {
                    biggestEntryLog.contains(it)
                }
            } else {
                false
            }
        }
        return haveSharedTransitionEntry
    }

    fun recordTransitionVisit(transitionIndex: Long, transitionWrapper: TransitionWrapper) {
        _visited.add(transitionWrapper)
        _participatedTransitionIndices.getOrPut(transitionWrapper) {
            sortedSetOf()
        }.add(transitionIndex)
        allParticipatedTransitionEntries.add(transitionIndex)
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